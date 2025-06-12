package com.securelogwatcher.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;
import com.securelogwatcher.service.MfaService;
import com.securelogwatcher.service.RefreshTokenService;
import com.securelogwatcher.domain.RefreshToken;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.LoginResponseDto;
import com.securelogwatcher.dto.MfaVerifyRequestDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {
    private final MfaService mfaService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/verify")
    public ApiResponseDto<?> verifyMfa(@RequestBody MfaVerifyRequestDto request) {

        Authentication authentication = mfaService.loadAuthentication(request.getUsername());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = (User) userDetails.getUser(); // Assuming CustomUserDetails holds User entity

        // Verify the MFA code using the appropriate strategy (TOTP or Email)
        boolean success = mfaService.verify(user, request.getCode());

        if (!success) {
            return new ApiResponseDto<>(false, "MFA verification failed.", null);
        }

        // --- MFA SUCCESSFULLY VERIFIED ---
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create and save new RefreshToken
        RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
        String refreshTokenString = newRefreshTokenEntity.getToken();

        // Generating new Access Token
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        // Returning full login response (access token + refresh token)
        return new ApiResponseDto<>(true, "MFA verification complete.",
                new LoginResponseDto(accessToken, refreshTokenString, request.getUsername()));
    }
}
