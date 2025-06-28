package com.securelogwatcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securelogwatcher.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
