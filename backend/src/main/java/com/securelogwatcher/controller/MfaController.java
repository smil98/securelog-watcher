package com.securelogwatcher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import com.securelogwatcher.service.MfaService;
import com.securelogwatcher.service.RefreshTokenService;

import jakarta.validation.Valid;

import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.RefreshToken;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.LoginResponseDto;
import com.securelogwatcher.dto.MfaEnrollResponseDto;
import com.securelogwatcher.dto.MfaVerifyRequestDto;
import com.securelogwatcher.exception.MfaVerificationException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {
    private final MfaService mfaService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enroll/initiate/{mfaType}")
    public ResponseEntity<ApiResponseDto<?>> initiateMfaEnrollment(@PathVariable MfaType mfaType,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = (User) userDetails.getUser();

        String setUpData = mfaService.initiateMfaEnrollment(user, mfaType);
        MfaEnrollResponseDto responseDto = new MfaEnrollResponseDto(
                mfaType,
                setUpData);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "MFA enrollment initiated successfully", responseDto));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDto<?>> verifyMfa(@RequestBody MfaVerifyRequestDto request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = (User) userDetails.getUser();

        // Verify the MFA code using the appropriate strategy (TOTP or Email)
        mfaService.verify(user, request.getCode());

        // --- MFA SUCCESSFULLY VERIFIED ---
        // Create and save new RefreshToken
        RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
        String refreshTokenString = newRefreshTokenEntity.getToken();

        // Generating new Access Token
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        LoginResponseDto responseDto = new LoginResponseDto(accessToken, refreshTokenString, user.getUsername());

        // Returning full login response (access token + refresh token)
        return ResponseEntity.ok(new ApiResponseDto<>(true, "MFA verification complete.", responseDto));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enroll/confirm")
    public ResponseEntity<ApiResponseDto<?>> confirmMfaEnrollment(
            @Valid @RequestBody MfaVerifyRequestDto request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        mfaService.confirmMfaEnrollment(user, request.getCode());
        return ResponseEntity
                .ok(new ApiResponseDto<>(true, "MFA enrollment confirmed successfully.", "MFA enabled."));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/disable")
    public ResponseEntity<ApiResponseDto<String>> disableMfa(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        mfaService.disableMfa(user);

        return ResponseEntity.ok(new ApiResponseDto<>(true, "MFA disabled successfully.", "MFA disabled."));
    }
}
