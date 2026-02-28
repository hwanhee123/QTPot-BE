package com.qttracker.domain.attendance;

import com.qttracker.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByMemberAndCreatedDate(Member member, LocalDate date);

    List<Attendance> findByMemberAndCreatedDateBetweenOrderByCreatedDateAsc(
            Member member, LocalDate start, LocalDate end);

    long countByMemberAndCreatedDateBetween(Member member, LocalDate start, LocalDate end);

    @Query("SELECT a.member.id, COUNT(a) FROM Attendance a " +
            "WHERE a.createdDate BETWEEN :start AND :end GROUP BY a.member.id")
    List<Object[]> countGroupByMember(@Param("start") LocalDate start,
                                      @Param("end")   LocalDate end);

    // 날짜별 전체 피드 (홈 화면용)
    List<Attendance> findByCreatedDateOrderByCreatedAtDesc(LocalDate date);

    // 관리자: 월별 전체 목록
    List<Attendance> findByCreatedDateBetweenOrderByCreatedAtDesc(LocalDate start, LocalDate end);

    // 피드: 월별 전체 (날짜 최신순, 같은 날은 업로드 최신순)
    List<Attendance> findByCreatedDateBetweenOrderByCreatedDateDescCreatedAtDesc(LocalDate start, LocalDate end);

    // 전체 최신 피드 (홈 화면 기본)
    List<Attendance> findAllByOrderByCreatedAtDesc();

    // 내 전체 인증 목록 (등록순)
    List<Attendance> findByMemberOrderByCreatedAtDesc(Member member);

}
