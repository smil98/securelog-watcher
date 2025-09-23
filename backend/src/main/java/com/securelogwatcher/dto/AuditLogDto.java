package com.securelogwatcher.dto;

import java.time.Instant;

import com.securelogwatcher.domain.AuditEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuditLogDto {
    private Long id;
    private Instant timestamp;
    private AuditEventType eventType;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String details;
}