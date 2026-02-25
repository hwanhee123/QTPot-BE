package com.qttracker.domain.member;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "member")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // 비밀번호 변경용 (관리자 초기화, 본인 변경 모두 사용)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public enum Role { USER, LEADER, ADMIN }
}
