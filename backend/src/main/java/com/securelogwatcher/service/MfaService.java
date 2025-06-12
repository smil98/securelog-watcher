package com.securelogwatcher.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

import com.securelogwatcher.mfa.MfaVerificationStrategy;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.MfaType;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.security.CustomUserDetails;
import com.securelogwatcher.security.JwtTokenProvider;
import com.securelogwatcher.exception.MfaVerificationException;

@Service
@RequiredArgsConstructor
public class MfaService {
    private final UserRepository userRepository;
    private final Map<MfaType, MfaVerificationStrategy> strategyMap = new HashMap<>();
    private final List<MfaVerificationStrategy> strategies;
    private final JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    public void init() {
        for (MfaVerificationStrategy strategy : strategies) {
            strategyMap.put(strategy.getMfaType(), strategy);
        }
    }

    public boolean verify(User user, String code) {
        MfaVerificationStrategy strategy = strategyMap.get(user.getMfaType());
        if (strategy == null) {
            throw new MfaVerificationException("Unsupported MFA type: " + user.getMfaType());
        }
        return strategy.verify(user, code);
    }

    public Authentication loadAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new DisabledException("User is not active or enabled");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

    public String createMfaToken(Authentication authentication) {
        return jwtTokenProvider.createMfaToken(authentication);
    }
}
