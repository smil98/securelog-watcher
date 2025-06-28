package com.securelogwatcher.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.securelogwatcher.domain.PasswordResetToken;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.exception.CustomAuthenticationException;
import com.securelogwatcher.repository.PasswordResetTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    @Value("${security.password-reset-token-expiration:30}")
    private final long PASSWORD_RESET_TOKEN_EXPIRATION;
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenService.class);
    private final AuditLogService auditLogService;

    @Transactional
    public PasswordResetToken createToken(User user) {
        passwordResetTokenRepository.deleteByUser(user);

        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(PASSWORD_RESET_TOKEN_EXPIRATION, ChronoUnit.MINUTES);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        return passwordResetTokenRepository.save(passwordResetToken);
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }

    @Transactional

    public void deleteExpiredTokens() {
        int deletedCount = passwordResetTokenRepository.deleteByExpiryDateBefore(Instant.now());
        auditLogService.logTokenCleanup(deletedCount, "password reset");
        logger.info("Cleaned up {} expired password reset tokens.", deletedCount);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduleExpiredTokenCleanup() {
        logger.info("Starting scheduled cleanup of expired password reset tokens...");
        deleteExpiredTokens();
        logger.info("Finished scheduled cleanup of expired password reset tokens.");
    }

    public User validateToken(String token) {
        PasswordResetToken resetToken = findByToken(token)
                .orElseThrow(() -> new CustomAuthenticationException("Invalid or expired password reset token."));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            deleteToken(resetToken);
            throw new CustomAuthenticationException("Invalid or expired password reset token.");
        }

        return resetToken.getUser();
    }
}
