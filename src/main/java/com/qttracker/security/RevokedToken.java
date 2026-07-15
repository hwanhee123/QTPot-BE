package com.qttracker.security;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 로그아웃한 JWT의 jti를 만료 시각까지만 저장해 재사용을 막는다.
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "revoked_token")
public class RevokedToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
