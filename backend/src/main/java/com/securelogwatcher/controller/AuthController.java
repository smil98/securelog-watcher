package com.securelogwatcher.controller;

import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.LoginRequestDto;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<?>> createUser(@RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(authService.registerUser(signupRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<?>> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequestDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<?>> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Refresh token is missing or malformed.");
        }
        String refreshTokenString = refreshTokenHeader.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshTokenString));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logout(@RequestHeader("Authorization") String refreshTokenHeader) {
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Refresh token is missing or malformed.");
        }
        String refreshTokenString = refreshTokenHeader.substring(7);
        return ResponseEntity.ok(authService.logout(refreshTokenString));
    }
}
