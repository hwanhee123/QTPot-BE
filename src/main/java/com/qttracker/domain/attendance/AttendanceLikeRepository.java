package com.qttracker.domain.attendance;

import com.qttracker.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceLikeRepository extends JpaRepository<AttendanceLike, Long> {
    Optional<AttendanceLike> findByAttendanceAndMember(Attendance attendance, Member member);
    long countByAttendance(Attendance attendance);
    void deleteByAttendance(Attendance attendance);
}
