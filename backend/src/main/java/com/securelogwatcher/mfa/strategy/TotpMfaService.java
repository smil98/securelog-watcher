package com.securelogwatcher.mfa.strategy;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.mfa.MfaVerificationException;
//import dev.samueltaylor.strategies.totp.Totp; // We'll add this dependency
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TotpMfaService implements MfaVerificationStrategy {

    @Override
    public MfaType getMfaType() {
        return MfaType.TOTP;
    }

    @Override
    public boolean verify(User user, String code) {
        // User entity must store the TOTP secret
        String totpSecret = user.getTotpSecret(); // Assuming User has getTotpSecret() method

        if (totpSecret == null || totpSecret.isEmpty()) {
            throw new MfaVerificationException("TOTP is not configured for this user.");
        }

        // Use the Totp library to verify the code
        // Totp totp = Totp.builder()
        // .secret(totpSecret)
        // .build();

        // return totp.verify(code);
        return true;
    }

    // public String generateNewTotpSecret() {
    // return Totp.generateSecret();
    // }
}
