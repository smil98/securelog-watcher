package com.securelogwatcher.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.VerificationCode;
import com.securelogwatcher.exception.MfaVerificationException;
import com.securelogwatcher.mfa.MfaVerificationStrategy;
import com.securelogwatcher.repository.VerificationCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailMfaService implements MfaVerificationStrategy {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final LoginAttemptService loginAttemptService;

    @Value("${spring.mail.username}")
    private String senderEmail;
    @Value("${spring.email.code-validity-seconds:900}")
    private long codeValiditySeconds;

    @Override
    public MfaType getMfaType() {
        return MfaType.EMAIL;
    }

    public void sendVerificationCode(User user) {
        String code = generateRandomCode(); // 6 digit code

        // 1. Clean up any existing code for the user
        verificationCodeRepository.deleteByUserId(user.getId()); // Important to avoid multiple active codes

        // 2. Save the new code
        verificationCodeRepository.save(new VerificationCode(user.getId(), code, expirationTime()));

        // 3. Email sending
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your MFA Verification Code");
        message.setText("Your verification code is: " + code + ". This code is valid for " + (codeValiditySeconds / 60)
                + " minutes.");
        message.setFrom(senderEmail);

        // Sending email
        try {
            mailSender.send(message);
            loginAttemptService.resetEmailSendAttempts(user.getUsername());
        } catch (MailException e) {
            loginAttemptService.recordEmailSendAttempt(user.getUsername());
            System.err.println("Failed to send verification email to " + user.getEmail() + ": " + e.getMessage());
            throw new MfaVerificationException("Failed to send verification email. Please try again later");
        }
    }

    @Override
    public boolean verify(User user, String code) {
        // Use Optional to handle cases where the code might not be found
        Optional<VerificationCode> storedCodeOptional = verificationCodeRepository.findByUserId(user.getId());

        if (storedCodeOptional.isEmpty()) {
            throw new MfaVerificationException("Verification code not found for this user.");
        }

        VerificationCode stored = storedCodeOptional.get();

        if (stored.isExpired()) {
            // Delete the expired code for cleanup
            verificationCodeRepository.delete(stored);
            throw new MfaVerificationException("Verification code expired. Please request a new one.");
        }

        boolean isValid = stored.getCode().equals(code);

        // Delete code after successful verification to prevent reuse
        if (isValid) {
            verificationCodeRepository.delete(stored);
            loginAttemptService.resetEmailSendAttempts(user.getUsername());
        } else {
            throw new MfaVerificationException("Invalid verification code."); // Specific error message for client
        }

        return isValid; // Return the actual verification result
    }

    private String generateRandomCode() {
        // 6 digit random
        SecureRandom secureRandom = new SecureRandom();
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private Instant expirationTime() {
        return Instant.now().plusSeconds(codeValiditySeconds);
    }
}
