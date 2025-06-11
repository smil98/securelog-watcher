package com.securelogwatcher.service;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.LoginRequestDto;
import com.securelogwatcher.dto.LoginResponseDto;
import com.securelogwatcher.dto.MfaRequiredResponseDto;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.exception.CustomAuthenticationException;
import com.securelogwatcher.exception.EmailAlreadyExistsException;
import com.securelogwatcher.exception.UsernameAlreadyExistsException;
import com.securelogwatcher.mfa.MfaTokenProvider;
import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;
import com.securelogwatcher.config.SecurityConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MfaTokenProvider mfaTokenProvider;

    public ApiResponseDto<?> registerUser(SignupRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("The username already exists");
        } else if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("The email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .mfaType(dto.getMfaType())
                .enabled(true)
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        return new ApiResponseDto<>(true, "Register complete. Please sign in to continue", null);
    }

    public ResponseEntity<?> authenticateUser(LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (userDetails.getMfaType() != MfaType.NONE) {
                // User that needs MFA Authentication
                String mfaToken = mfaTokenProvider.generateMfaToken(userDetails); // MFA token

                MfaRequiredResponseDto mfaDto = new MfaRequiredResponseDto(
                        userDetails.getUsername(),
                        userDetails.getMfaType());

                return ResponseEntity.ok(
                        new ApiResponseDto<>(
                                true,
                                "MFA required",
                                mfaDto));
            }

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            return ResponseEntity.ok(
                    new LoginResponseDto(accessToken, refreshToken, loginRequestDto.getUsername()));
        } catch (BadCredentialsException e) {
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }
}
