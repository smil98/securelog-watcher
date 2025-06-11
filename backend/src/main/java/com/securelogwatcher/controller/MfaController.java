package com.securelogwatcher.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securelogwatcher.security.JwtTokenProvider;
import com.securelogwatcher.dto.LoginResponseDto;
import com.securelogwatcher.dto.MfaVerifyRequestDto;
import com.securelogwatcher.mfa.MfaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {
    private final MfaService mfaService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyRequestDto request) {
        // boolean success = mfaService.verify(request.getUsername(),
        // request.getCode());
        // if (!success) {
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("MFA verification
        // failed");
        // }

        Authentication authentication = mfaService.loadAuthentication(request.getUsername());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken, request.getUsername()));
    }
}
