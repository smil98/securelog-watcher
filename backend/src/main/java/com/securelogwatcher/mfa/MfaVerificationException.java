package com.securelogwatcher.mfa;

public class MfaVerificationException extends RuntimeException {
    public MfaVerificationException(String message) {
        super(message);
    }
}