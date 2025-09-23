package com.securelogwatcher.controller;

import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.AuditLogDto;
import com.securelogwatcher.service.AuditLogService;
import com.securelogwatcher.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    // Accessible by Auditors and all roles above
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'SECURITY_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Accessible by Security Managers and above
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_MANAGER', 'SUPER_ADMIN')")
    @PostMapping("/users/{username}/toggle-enable")
    public ResponseEntity<ApiResponseDto<?>> toggleUserEnable(@PathVariable String username) {
        return ResponseEntity.ok(userService.toggleUserEnable(username));
    }

    // Accessible by Admins and above
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/users/{username}/change-role")
    public ResponseEntity<ApiResponseDto<?>> changeUserRole(@PathVariable String username,
            @RequestBody String newRole) {
        return ResponseEntity.ok(userService.changeUserRole(username, newRole));
    }

    // For viewing all audit logs
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'SECURITY_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }
}