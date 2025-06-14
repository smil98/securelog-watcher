package com.securelogwatcher.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.domain.RefreshToken;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.LoginRequestDto;
import com.securelogwatcher.dto.LoginResponseDto;
import com.securelogwatcher.dto.MfaRequiredResponseDto;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.exception.CustomAuthenticationException;
import com.securelogwatcher.exception.EmailAlreadyExistsException;
import com.securelogwatcher.exception.UsernameAlreadyExistsException;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final MfaService mfaService;

    public ApiResponseDto<?> registerUser(SignupRequestDto signupRequestDto) {
        if (userRepository.existsByUsername(signupRequestDto.getUsername())) {
            throw new UsernameAlreadyExistsException("The username already exists");
        } else if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("The email already exists");
        }

        User newUser = User.builder()
                .username(signupRequestDto.getUsername())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .email(signupRequestDto.getEmail())
                .mfaType(MfaType.NONE)
                .enabled(true)
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(newUser);
        return new ApiResponseDto<>(true, "Register complete. Please sign in to continue", null);
    }

    public ApiResponseDto<?> authenticateUser(LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            throw new CustomAuthenticationException(
                    "Account is temporarily locked due to too many failed login attempts. Please try again after "
                            + loginAttemptService.getBLOCK_DURATION_MINUTES() + " minutes.");
        }

        try {
            // 1. Authenticate username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (User) userDetails.getUser();

            // 2. Check MFA requirement
            if (user.getMfaType() != MfaType.NONE) {
                // User needs MFA authentication
                // Generate MFA token (used by frontend to call /api/mfa/verify)
                String mfaToken = mfaService.createMfaToken(authentication); // Delegates to JwtTokenProvider if JWT
                                                                             // mfaToken

                // Return MFA required response, including the mfaToken
                MfaRequiredResponseDto mfaDto = new MfaRequiredResponseDto(
                        userDetails.getUsername(),
                        user.getMfaType(), // Use user.getMfaType() as userDetails.getMfaType() may be derived
                        mfaToken);

                return new ApiResponseDto<>(true, "MFA required", mfaDto);
            }

            // 3. Full Authentication (MFA not enabled or not required)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Create and save RefreshToken entity
            // The RefreshTokenService sets the 'lastMfaVerificationTime' to now
            RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
            String refreshToken = newRefreshTokenEntity.getToken();

            // Create and return Access Token
            String accessToken = jwtTokenProvider.createAccessToken(authentication);

            return new ApiResponseDto<>(true, "Authentication Complete",
                    new LoginResponseDto(accessToken, refreshToken, loginRequestDto.getUsername()));
        } catch (BadCredentialsException e) {
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }

    public ApiResponseDto<?> refreshToken(String refreshTokenString) {
        // 1. Find the refresh token in the database
        RefreshToken existingRefreshToken = refreshTokenService.findByToken(refreshTokenString)
                .orElseThrow(() -> new CustomAuthenticationException("Invalid refresh token."));

        // 2. Validate the refresh token's expiration and MFA re-verification
        // requirement
        RefreshToken validatedToken = refreshTokenService.verifyExpirationAndMfaRequirement(existingRefreshToken);

        // 3. Get the associated user
        User user = validatedToken.getUser();
        // Create an Authentication object for the user (needed for creating new JWTs)
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // No credentials needed here
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Generate a new Access Token
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);

        // 5. Generate a new Refresh Token (rotating refresh tokens)
        RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
        String newRefreshTokenString = newRefreshTokenEntity.getToken();

        // 6. Return the new tokens
        return new ApiResponseDto<>(true, "Token refreshed successfully.",
                new LoginResponseDto(newAccessToken, newRefreshTokenString, user.getUsername()));
    }

    public ApiResponseDto<?> changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomAuthenticationException("Incorrect old password.");
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        userRepository.save(user);
        return new ApiResponseDto<>(true, "Password changed successfully", null);
    }

    public ApiResponseDto<String> logout(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new CustomAuthenticationException("Refresh token not found."));
        refreshTokenService.delete(token);
        return new ApiResponseDto<>(true, "Logged out successfully.", null);
    }
}
