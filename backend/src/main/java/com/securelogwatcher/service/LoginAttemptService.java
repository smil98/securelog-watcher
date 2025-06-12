package com.securelogwatcher.service;

import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;

@Service
@Getter
public class LoginAttemptService {
    private final Cache<String, Integer> attemptsCache;

    @Value("${security.login-attempts.max-attempts}")
    private int MAX_ATTEMPTS;

    @Value("${security.login-attempts.block-duration-minutes}")
    private int BLOCK_DURATION_MINUTES;

    // Initializing cache with expiration based on the block duration
    public LoginAttemptService() {
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    public void recordFailedLogin(String username) {
        int attempts = attemptsCache.get(username, k -> 0); // Get current count or 0 if new
        attempts++;
        attemptsCache.put(username, attempts);
    }

    public void recordSuccessfulLogin(String username) {
        attemptsCache.invalidate(username); // Remove the entry to reset attempts
    }

    public boolean isBlocked(String username) {
        Integer attempts = attemptsCache.getIfPresent(username);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

}
