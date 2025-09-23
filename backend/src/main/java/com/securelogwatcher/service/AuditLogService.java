package com.securelogwatcher.service;

import com.securelogwatcher.domain.AuditLog;
import com.securelogwatcher.dto.AuditLogDto;
import com.securelogwatcher.domain.AuditEventType;
import com.securelogwatcher.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    private Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
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

    public void logEvent(AuditEventType eventType, String username, String details) {
        HttpServletRequest request = getCurrentHttpRequest().orElse(null);
        String ip = (request != null) ? getClientIp(request) : null;
        String userAgent = (request != null) ? getUserAgent(request) : null;

        AuditLog log = AuditLog.builder()
                .timestamp(Instant.now())
                .eventType(eventType)
                .username(username)
                .ipAddress(ip)
                .userAgent(userAgent)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    public void logLoginSuccess(String username) {
        logEvent(AuditEventType.LOGIN_SUCCESS, username, "User logged in successfully.");
    }

    public void logLoginFailure(String username, String errorMessage) {
        logEvent(AuditEventType.LOGIN_FAILURE, username, "Login failed: " + errorMessage);
    }

    public void logPasswordChangeAttempt(String username) {
        logEvent(AuditEventType.PASSWORD_CHANGE_ATTEMPT, username, "User initiated password change attempt.");
    }

    public void logPasswordChangeSuccess(String username) {
        logEvent(AuditEventType.PASSWORD_CHANGE_SUCCESS, username, "User successfully changed password.");
    }

    public void logPasswordChangeFailure(String username, String errorMessage) {
        logEvent(AuditEventType.PASSWORD_CHANGE_FAILURE, username, "Password change failed: " + errorMessage);
    }

    public void logPasswordResetRequest(String email) {
        logEvent(AuditEventType.PASSWORD_RESET_REQUEST, email, "Password reset link requested.");
    }

    public void logPasswordResetSuccess(String username) {
        logEvent(AuditEventType.PASSWORD_RESET_SUCCESS, username, "Password successfully reset via token.");
    }

    public void logPasswordResetFailure(String username, String token, String errorMessage) {
        logEvent(AuditEventType.PASSWORD_RESET_FAILURE, username,
                "Password reset failed for token: " + token + ". Reason: " + errorMessage);
    }

    public void logTokenCleanup(int count, String tokenType) {
        logEvent(AuditEventType.TOKEN_CLEANUP, null, "Cleaned up " + count + " expired " + tokenType + " tokens.");
    }

    public void logUserManagementAction(String actingUsername, String details) {
        logEvent(AuditEventType.USER_MANAGEMENT, actingUsername, details);
    }

    public List<AuditLogDto> getAllAuditLogs() {

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        return auditLogs.stream()
                .map(log -> AuditLogDto.builder()
                        .id(log.getId())
                        .timestamp(log.getTimestamp())
                        .eventType(log.getEventType())
                        .username(log.getUsername())
                        .ipAddress(log.getIpAddress())
                        .userAgent(log.getUserAgent())
                        .details(log.getDetails())
                        .build())
                .collect(Collectors.toList());
    }

}