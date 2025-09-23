package com.securelogwatcher.service;

import com.securelogwatcher.domain.PasswordResetToken;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.service.AuthService;
import com.securelogwatcher.service.PasswordResetTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private PasswordResetToken validToken;
    private static final String NEW_PASSWORD = "newPassword123";

    @BeforeEach
    void setUp() {
        // Create a test user instance
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        validToken = PasswordResetToken.builder()
                .token("validToken")
                .user(testUser)
                .expiryDate(Instant.now().plus(30, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void whenValidTokenAndNewPassword_thenPasswordIsReset() {
        when(tokenService.validateToken(validToken.getToken())).thenReturn(testUser);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.resetPassword(validToken.getToken(), NEW_PASSWORD);

        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(tokenService).deleteToken(any(PasswordResetToken.class));
        verify(userRepository).save(testUser);
        assertThat(testUser.getPassword()).isEqualTo("hashedNewPassword");
    }

    @Test
    void whenInvalidToken_thenExceptionIsThrown() {
        when(tokenService.validateToken("invalidToken"))
                .thenThrow(new UsernameNotFoundException("Invalid or expired token"));

        assertThrows(UsernameNotFoundException.class, () -> authService.resetPassword("invalidToken", NEW_PASSWORD));
        verify(userRepository, never()).save(any(User.class));
    }
}
