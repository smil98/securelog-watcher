package com.securelogwatcher.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securelogwatcher.domain.RefreshToken;
import com.securelogwatcher.domain.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
