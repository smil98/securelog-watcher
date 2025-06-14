package com.securelogwatcher.security;

import lombok.RequiredArgsConstructor;
import com.securelogwatcher.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import com.securelogwatcher.exception.CustomAuthenticationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.io.Decoders;

import java.security.Key;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessTokenValidity;

    @Value("${jwt.mfa-expiration}")
    private long mfaTokenValidity;

    private Key key;
    private final CustomUserDetailsService customUserDetailsService;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Create Token
    private String generateToken(Claims claims, long validityInMs) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Role role = ((CustomUserDetails) authentication.getPrincipal()).getUser().getRole();

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", role);

        return generateToken(claims, accessTokenValidity);
    }

    public String createMfaToken(Authentication authentication) {
        String username = authentication.getName();

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("tokenType", "mfa_challenge");
        claims.put("mfaType", ((CustomUserDetails) authentication.getPrincipal()).getMfaType().name());

        return generateToken(claims, mfaTokenValidity);
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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