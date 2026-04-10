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

    // 댓글 알림 켜기/끄기 (기본값 true)
    @Column(name = "comment_noti_enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean commentNotiEnabled = true;

    // 큐티 알림 시간 (HH:mm 형식, null이면 알림 안 보냄)
    @Column(name = "qt_noti_time", length = 5)
    private String qtNotiTime;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateNotificationSettings(boolean commentNotiEnabled, String qtNotiTime) {
        this.commentNotiEnabled = commentNotiEnabled;
        this.qtNotiTime = qtNotiTime;
    }

    public enum Role { USER, LEADER, ADMIN }
}
