package com.qttracker.domain.attendance;

import com.qttracker.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_like",
       uniqueConstraints = @UniqueConstraint(columnNames = {"attendance_id", "member_id"}))
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
