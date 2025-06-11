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
    public ApiResponseDto<?> createUser(@RequestBody SignupRequestDto signupRequestDto) {
        return authService.registerUser(signupRequestDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        return authService.authenticateUser(loginRequestDto);
    }

}
