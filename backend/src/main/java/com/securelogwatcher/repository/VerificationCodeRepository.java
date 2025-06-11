package com.securelogwatcher.repository;

import com.securelogwatcher.domain.VerificationCode; // Import your VerificationCode entity
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
