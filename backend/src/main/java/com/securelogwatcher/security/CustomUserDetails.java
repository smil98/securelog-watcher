package com.securelogwatcher.security;

import com.securelogwatcher.domain.User;
import com.securelogwatcher.domain.MfaType;
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
        return Collections.singletonList(user.getRole());
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    public MfaType getMfaType() {
        return user.getMfaType();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive(); // enabled && !deleted && !forceLoggedOut
    }

    // Not used is all set as true
    @Override
    public boolean isAccountNonExpired() {
        return true; // account expiration not used
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // no lock options
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // credential expiration does not exist
    }
}
