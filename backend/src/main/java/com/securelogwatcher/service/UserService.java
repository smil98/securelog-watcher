package com.securelogwatcher.service;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.dto.SignupRequestDto;
import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.mapper.UserMapper;
import com.securelogwatcher.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
}
