package com.securelogwatcher.service;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.exception.MfaVerificationException;
import com.securelogwatcher.mfa.MfaVerificationStrategy;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TotpMfaService implements MfaVerificationStrategy {

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    @Override
    public MfaType getMfaType() {
        return MfaType.TOTP;
    }

    @Override
    public boolean verify(User user, String code) {
        String totpSecret = user.getTotpSecret();

        if (totpSecret == null || totpSecret.isEmpty()) {
            throw new MfaVerificationException("TOTP is not configured for this user.");
        }

        boolean isValid = codeVerifier.isValidCode(totpSecret, code);

        if (!isValid) {
            throw new MfaVerificationException("Invalid TOTP code.");
        }

        return isValid;
    }

    public String generateNewTotpSecret() {
        return secretGenerator.generate();
    }
}
