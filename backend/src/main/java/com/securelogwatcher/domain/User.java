package com.securelogwatcher.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // for security reasons, exclude password from toString
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // MFA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaType mfaType; // NONE, EMAIL, TOTP

    @Column
    private String totpSecret; // for Google Authenticator

    @Column(nullable = false)
    private boolean mfaVerified; // for MFA

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false; // whether the account is deleted

    @Column(nullable = false)
    @Builder.Default
    private boolean forceLoggedOut = false; // whether the user is forced to log out (admin action)

    @Column(nullable = false)
    private boolean enabled; // eligible for login

    public boolean isActive() {
        return enabled && !deleted && !forceLoggedOut;
    }
}
