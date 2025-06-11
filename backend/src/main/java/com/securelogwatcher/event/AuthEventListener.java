package com.securelogwatcher.event;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.securelogwatcher.domain.LoginEventLog;
import com.securelogwatcher.repository.LoginEventLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthEventListener {
    private final LoginEventLogRepository logRepository;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) { // TODO: check Obsidian 6.11
        handleLoginEvent(event.getAuthentication(), true);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        handleLoginEvent(event.getAuthentication(), false);
    }

    private void handleLoginEvent(Authentication authentication, boolean isSuccess) {
        String username = authentication.getName();
        HttpServletRequest request = getCurrentHttpRequest();

        String ip = getClientIp(request);
        String userAgent = getUserAgent(request);

        LoginEventLog log = LoginEventLog.builder()
                .username(username)
                .success(isSuccess)
                .ip(ip)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        logRepository.save(log);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No current HTTP request available");
        }
        return attrs.getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
