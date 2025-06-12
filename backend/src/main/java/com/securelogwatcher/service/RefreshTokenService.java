package com.securelogwatcher.service;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.exception.CustomAuthenticationException;
import com.securelogwatcher.domain.RefreshToken;
import com.securelogwatcher.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.time.temporal.ChronoUnit;
import java.time.Instant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refreshTokenExpirationMinutes}")
    private long refreshTokenExpirationMinutes;

    @Value("${jwt.mfa.reVerificationMonths}")
    private long mfaReVerificationMonths;

    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(refreshTokenExpirationMinutes, ChronoUnit.MINUTES)) // Set expiration
                .lastMfaVerificationTime(Instant.now()) // User either just logged in or just completed MFA
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpirationAndMfaRequirement(RefreshToken token) {
        // 1. Check if the refresh token's inherent expiration date has passed
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token); // Delete the expired token from the database
            throw new CustomAuthenticationException("Refresh token was expired. Please log in again.");
        }

        // 2. Check the periodic MFA re-verification requirement
        // Calculate days since last MFA verification
        long daysSinceLastMfa = ChronoUnit.DAYS.between(token.getLastMfaVerificationTime(), Instant.now());
        // Convert configured months to days (approximate, 1 month = 30 days)
        long requiredMfaReVerificationDays = mfaReVerificationMonths * 30;

        if (daysSinceLastMfa >= requiredMfaReVerificationDays) {
            refreshTokenRepository.delete(token); // Invalidate the token as MFA re-verification is needed
            throw new CustomAuthenticationException("MFA re-verification required. Please log in again.");
        }

        return token;
    }

    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
