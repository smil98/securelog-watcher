package com.securelogwatcher.exception;

public class MfaVerificationException extends RuntimeException {
    public MfaVerificationException(String message) {
        super(message);
    }
}