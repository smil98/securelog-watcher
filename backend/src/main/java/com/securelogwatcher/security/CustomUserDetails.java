package com.securelogwatcher.security;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
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
        return Collections.singletonList(user.getRoles());
    }

    @Override
    public String getPassword() {
        return null; // no password is stored in this class for security reasons
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // no account expiration is used in this implementation
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // no account lock is used in this implementation
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // no credentials expiration is used in this implementation
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}
