package com.securelogwatcher.security;

import com.securelogwatcher.domain.Role;
import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final String USER_NOT_FOUND = "Invalid username or password";

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    public CustomUserDetails loadUserById(Long id) {
        return userRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
