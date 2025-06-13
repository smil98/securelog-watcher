package com.securelogwatcher.config;

import org.springframework.context.annotation.Configuration;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class TotpConfig {

    @Value("${totp.time.period:60}")
    private int totpTimePeriod;
    @Value("${totp.time.discrepancy:2}")
    private int totpTimeDiscrepancy;

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator();
    }

    @Bean
    public QrGenerator qrGenerator() {
        return new ZxingPngQrGenerator();
    }

    @Bean
    public CodeVerifier codeVerifier(TimeProvider timeProvider) {
        // SHA256, 6 digits, 60 sec period, 2 window (discrepancy)
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(HashingAlgorithm.SHA256),
                timeProvider);
        verifier.setTimePeriod(totpTimePeriod);
        verifier.setAllowedTimePeriodDiscrepancy(totpTimeDiscrepancy);
        return verifier;
    }
}
