package com.securelogwatcher.security;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> (GrantedAuthority) () -> role.name())
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return null; // 비밀번호는 사용하지 않음
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부는 사용하지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부는 사용하지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부는 사용하지 않음
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}
