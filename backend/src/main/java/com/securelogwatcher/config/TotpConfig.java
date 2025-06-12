package com.securelogwatcher.config;

import org.springframework.context.annotation.Configuration;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import org.springframework.context.annotation.Bean;

@Configuration
public class TotpConfig {

    // @Value("${totp.time.period}")
    // private int totpTimePeriod;
    // @Value("${totp.time.discrepancy}")
    // private int totpTimeDiscrepency;

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator();
    }

    @Bean
    public CodeVerifier codeVerifier(TimeProvider timeProvider) {
        // SHA1, 6 digits, 30 sec period, 1 window (discrepancy)
        return new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);
    }

}
