package com.securelogwatcher.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

import com.securelogwatcher.mfa.MfaVerificationStrategy;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;
import com.securelogwatcher.exception.MfaVerificationException;
import com.securelogwatcher.repository.VerificationCodeRepository;

@Service
@RequiredArgsConstructor
public class MfaService {
    private final UserRepository userRepository;
    private final Map<MfaType, MfaVerificationStrategy> strategyMap = new HashMap<>();
    private final List<MfaVerificationStrategy> strategies;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeRepository verificationCodeRepository;

    @PostConstruct
    public void init() {
        for (MfaVerificationStrategy strategy : strategies) {
            strategyMap.put(strategy.getMfaType(), strategy);
        }
    }

    public boolean verify(User user, String code) {
        MfaVerificationStrategy strategy = strategyMap.get(user.getMfaType());
        if (strategy == null) {
            throw new MfaVerificationException("Unsupported MFA type: " + user.getMfaType());
        }
        return strategy.verify(user, code);
    }

    public Authentication loadAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new DisabledException("User is not active or enabled");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

    public String createMfaToken(Authentication authentication) {
        return jwtTokenProvider.createMfaToken(authentication);
    }

    public String initiateMfaEnrollment(User user, MfaType mfaType) {
        if (mfaType == MfaType.NONE) {
            throw new MfaVerificationException("MFA type NONE cannot be enrolled.");
        }

        MfaVerificationStrategy strategy = strategyMap.get(mfaType);
        if (strategy == null) {
            throw new MfaVerificationException("No strategy found for MFA type: " + mfaType);
        }

        if (user.getMfaType() != MfaType.NONE && user.getMfaType() != mfaType) {
            disableMfa(user); // Disable previous MFA if user is switching type
        }

        user.setMfaType(mfaType);
        userRepository.save(user);

        return strategy.initiateEnrollment(user);
    }

    public boolean confirmMfaEnrollment(User user, String code) {
        if (user.getMfaType() == MfaType.NONE) {
            throw new MfaVerificationException("MFA is not enabled for this user. Please initiate enrollment first.");
        }
        return verify(user, code);
    }

    public void disableMfa(User user) {
        user.setMfaType(MfaType.NONE);

        // Clear TOTP secret if it exists
        if (user.getTotpSecret() != null) {
            user.setTotpSecret(null);
        }
        // Delete any associated email verification codes
        verificationCodeRepository.deleteByUserId(user.getId());
        userRepository.save(user); // Save the updated user
    }

}
