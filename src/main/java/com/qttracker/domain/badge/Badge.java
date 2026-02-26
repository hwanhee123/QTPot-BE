package com.qttracker.domain.badge;

import com.qttracker.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "badge", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "month"})
})
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String month;       // YYYY-MM

    @Column(name = "badge_name")
    private String badgeName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void upgradeBadgeName(String newBadgeName) {
        this.badgeName = newBadgeName;
    }
}
