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

    // 관리자용 날짜별 전체 조회 (비공개 포함) — /api/admin/attendance
    List<Attendance> findByCreatedDateOrderByCreatedAtDesc(LocalDate date);

    // 관리자: 월별 전체 목록
    List<Attendance> findByCreatedDateBetweenOrderByCreatedAtDesc(LocalDate start, LocalDate end);

    // 멤버 피드: 날짜별 (비공개는 작성자 본인에게만 노출)
    @Query("SELECT a FROM Attendance a WHERE a.createdDate = :date " +
            "AND (a.isPrivate = false OR a.member = :me) ORDER BY a.createdAt DESC")
    List<Attendance> findFeedByDate(@Param("date") LocalDate date, @Param("me") Member me);

    // 멤버 피드: 월별 (날짜 최신순, 같은 날은 업로드 최신순, 비공개는 작성자 본인에게만 노출)
    @Query("SELECT a FROM Attendance a WHERE a.createdDate BETWEEN :start AND :end " +
            "AND (a.isPrivate = false OR a.member = :me) " +
            "ORDER BY a.createdDate DESC, a.createdAt DESC")
    List<Attendance> findFeedByMonth(@Param("start") LocalDate start,
                                      @Param("end")   LocalDate end,
                                      @Param("me")    Member me);

    // 전체 최신 피드 (홈 화면 기본)
    List<Attendance> findAllByOrderByCreatedAtDesc();

    // 내 전체 인증 목록 (등록순)
    List<Attendance> findByMemberOrderByCreatedAtDesc(Member member);

}
