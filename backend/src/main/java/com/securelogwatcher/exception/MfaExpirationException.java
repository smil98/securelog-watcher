package com.securelogwatcher.exception;

public class MfaExpirationException extends RuntimeException {
    private String mfaToken;

    public MfaExpirationException(String message, String mfaToken) {
        super(message);
        this.mfaToken = mfaToken;
    }

    public String getMfaToken() {
        return mfaToken;
    }
}
