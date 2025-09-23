package com.securelogwatcher.service;

import com.securelogwatcher.domain.AuditLog;
import com.securelogwatcher.domain.AuditEventType;
import com.securelogwatcher.repository.AuditLogRepository;
import com.securelogwatcher.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void whenLoggingEvent_thenAuditLogIsSaved() {
        String username = "testuser";
        String details = "Test details";

        auditLogService.logEvent(AuditEventType.LOGIN_SUCCESS, username, details);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
