package com.securelogwatcher.mfa;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.User;

public interface MfaVerificationStrategy {
    MfaType getMfaType();

    boolean verify(User user, String code);

    String initiateEnrollment(User user);
}
