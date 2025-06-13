package com.securelogwatcher.service;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.exception.MfaVerificationException;
import com.securelogwatcher.mfa.MfaVerificationStrategy;
import com.securelogwatcher.repository.UserRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TotpMfaService implements MfaVerificationStrategy {

    private final UserRepository userRepository;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;

    @Value("${totp.issuer:SecureLog_Watcher}")
    private String issuer;
    @Value("${totp.time.period:60}")
    private int totpTimePeriod;
    @Value("${totp.time.discrepancy:2}")
    private int totpTimeDiscrepancy;

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

    public String generateSecretAndQrUri(User user) {
        String secret = secretGenerator.generate();
        user.setTotpSecret(secret); // Set the secret for the user
        userRepository.save(user); // Save the updated user with the new secret

        // Build QR data
        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA256)
                .digits(6)
                .period(totpTimePeriod)
                .build();

        try {
            // Generate QR code image and convert to Base64 data URI
            byte[] imageData = qrGenerator.generate(data);
            String mimeType = qrGenerator.getImageMimeType();
            String dataUri = Utils.getDataUriForImage(imageData, mimeType);
            return dataUri;
        } catch (QrGenerationException e) {
            throw new MfaVerificationException("Failed to generate TOTP QR code: " + e);
        }
    }

    public String generateNewTotpSecret() {
        return secretGenerator.generate();
    }
}
