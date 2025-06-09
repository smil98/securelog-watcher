package com.securelogwatcher.controller;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

//로그인, 토큰 재발급 등
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;

    @PostMapping("/login")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

}
