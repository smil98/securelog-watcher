package com.securelogwatcher.security;

import com.securelogwatcher.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import main.java.com.securelogwatcher.exception.CustomAuthenticationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_EXPRIATION_TIME}")
    private long validityInMilliseconds;

    private Key key;
    private final CustomUserDetailsService customUserDetailsService;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Create Token
    public String createToken(String username, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toSet()));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // get authentication from token
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // checking user status (deactivated account, deleted account, forced logout)
        if (!userDetails.isEnabled()) {
            throw new CustomAuthenticationException("User account is disabled");
        }

        if (!userDetails.isAccountNonLocked()) {
            throw new CustomAuthenticationException("User account is locked");
        }

        if (!userDetails.isAccountNonExpired()) {
            throw new CustomAuthenticationException("User account is expired");
        }

        if (!userDetails.isCredentialsNonExpired()) {
            throw new CustomAuthenticationException("User credentials have expired");
        }

        // creating UsernamePasswordAuthenticationToken
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}