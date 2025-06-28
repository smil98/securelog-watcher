package com.securelogwatcher.event;

import com.securelogwatcher.service.AuditLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.securelogwatcher.service.LoginAttemptService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthEventListener {
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) { // TODO: check Obsidian 6.11
        String username = event.getAuthentication().getName();
        loginAttemptService.recordSuccessfulLogin(username);
        auditLogService.logLoginSuccess(username);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String errorMessage = event.getException().getMessage();
        if (event.getException() instanceof BadCredentialsException) {
            loginAttemptService.recordFailedLogin(username);
        }
        auditLogService.logLoginFailure(username, errorMessage);
    }
}
