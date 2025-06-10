package com.securelogwatcher.service;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.exception.EmailAlreadyExistsException;
import com.securelogwatcher.exception.UsernameAlreadyExistsException;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                .roles(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        return new ApiResponseDto<>(true, "Register complete. Please sign in to continue", null);
    }
}
