package com.securelogwatcher.domain;

import com.securelogwatcher.domain.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;

    @Column(nullable = false)
    private boolean enabled;

    // MFA 관련 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaType mfaType; // NONE, EMAIL, TOTP

    @Column
    private String emailForMfa; // 이메일 OTP용

    @Column
    private String totpSecret; // Google Authenticator 연동용 시크릿

    @Column(nullable = false)
    private boolean mfaVerified; // 로그인 중 MFA 인증 완료 여부

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
