package com.securelogwatcher.domain;

import jakarta.persistence.*; // Use jakarta.persistence for JPA annotations
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "verification_codes") // Optional: define table name
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private Instant expiryTime;

    // Custom constructor for convenience
    public VerificationCode(Long userId, String code, Instant expiryTime) {
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryTime);
    }
}
