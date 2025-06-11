package com.securelogwatcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securelogwatcher.domain.LoginEventLog;

public interface LoginEventLogRepository extends JpaRepository<LoginEventLog, Long> {

}
