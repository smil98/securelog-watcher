package com.securelogwatcher.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.dto.UserDto;
import com.securelogwatcher.mapper.UserMapper;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.exception.CustomAuthenticationException;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .mfaType(user.getMfaType())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiResponseDto<?> toggleUserEnable(String username) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find the user to modify
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // Add a security check: A user cannot disable themselves
        if (username.equals(currentUsername)) {
            throw new AccessDeniedException("You cannot disable your own account.");
        }

        // Prevent disabling a SUPER_ADMIN account
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new AccessDeniedException("SUPER_ADMIN accounts cannot be disabled.");
        }

        // Toggle the 'enabled' status
        boolean newStatus = !user.isEnabled();
        user.setEnabled(newStatus);
        userRepository.save(user);

        // Log the event
        String details = "Account for user '" + username + "' was " + (newStatus ? "enabled." : "disabled.");
        auditLogService.logUserManagementAction(currentUsername, details);

        return new ApiResponseDto<>(true, details, null);
    }

    @Transactional
    public ApiResponseDto<?> changeUserRole(String username, String newRoleStr) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validate the new role string
        Role newRole;
        try {
            newRole = Role.valueOf(newRoleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role provided: " + newRoleStr);
        }

        // Find the user to modify
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // Add a security check: SUPER_ADMIN role can only be assigned by a SUPER_ADMIN
        boolean isCurrentSuperAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals(Role.ROLE_SUPER_ADMIN.getAuthority()));

        if (newRole == Role.ROLE_SUPER_ADMIN && !isCurrentSuperAdmin) {
            throw new AccessDeniedException("Only a SUPER_ADMIN can assign the SUPER_ADMIN role.");
        }

        if (user.getRole() == Role.ROLE_SUPER_ADMIN && !isCurrentSuperAdmin) {
            throw new AccessDeniedException("Only a SUPER_ADMIN can change the role of a SUPER_ADMIN.");
        }

        // Prevent a user from changing their own role (could lead to privilege
        // escalation)
        if (username.equals(currentUsername)) {
            throw new AccessDeniedException("You cannot change your own role.");
        }

        // Change the user's role
        user.setRole(newRole);
        userRepository.save(user);

        // Log the event
        String details = "Role for user '" + username + "' was changed to '" + newRole + "'.";
        auditLogService.logUserManagementAction(currentUsername, details);

        return new ApiResponseDto<>(true, details, null);
    }
}
