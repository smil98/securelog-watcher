package com.securelogwatcher.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.mapper.UserMapper;
import com.securelogwatcher.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerUser(SignupRequestDto dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .mfaType(dto.getMfaType())
                .enabled(true)
                .build();
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
