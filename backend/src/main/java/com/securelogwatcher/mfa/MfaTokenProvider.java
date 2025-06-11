package com.securelogwatcher.mfa;

import com.securelogwatcher.security.CustomUserDetails;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MfaTokenProvider {

    public String generateMfaToken(CustomUserDetails customUserDetails) {
        return "TODO";
    }
}
