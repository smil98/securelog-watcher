package com.securelogwatcher.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_AUDITOR,
    ROLE_SECURITY_MANAGER,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}