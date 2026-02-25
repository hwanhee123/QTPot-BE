package com.qttracker.domain.attendance;

import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import com.qttracker.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository      attendanceRepo;
    private final AttendanceImageRepository imageRepo;
    private final MemberRepository          memberRepo;
    private final S3Uploader                s3Uploader;
    private final CommentRepository         commentRepo;

    // ── 다중 사진 업로드 (하루 1개 게시글, 사진 N장)
    @Transactional
    public AttendanceResponse upload(
            String email, List<MultipartFile> images, String content, LocalDate date, boolean isPrivate) {
        Member member = find(email);
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        if (attendanceRepo.existsByMemberAndCreatedDate(member, targetDate))
            throw new IllegalStateException("해당 날짜에 이미 큐티 인증을 완료했습니다.");
        if (images == null || images.isEmpty())
            throw new IllegalArgumentException("사진을 최소 1장 선택해주세요.");

        // 게시글 먼저 저장
        Attendance attendance = attendanceRepo.save(
                Attendance.builder()
                        .member(member)
                        .content(content)
                        .isPrivate(isPrivate)
                        .createdDate(targetDate)
                        .build());

        // 사진 S3 업로드 후 AttendanceImage 저장
        for (MultipartFile image : images) {
            String url = s3Uploader.upload(image, "attendance");
            imageRepo.save(AttendanceImage.builder()
                    .attendance(attendance)
                    .imageUrl(url)
                    .build());
        }

        // 이미지 포함해서 다시 조회 후 반환
        return new AttendanceResponse(
                attendanceRepo.findById(attendance.getId()).orElseThrow());
    }

    // ── 소감 수정 (본인만)
    @Transactional
    public AttendanceResponse updateContent(
            String email, Long attendanceId, ContentUpdateRequest req) {
        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (!attendance.getMember().getEmail().equals(email))
            throw new IllegalStateException("본인의 게시글만 수정할 수 있습니다.");
        attendance.updateContent(req.getContent());
        return new AttendanceResponse(attendance);
    }

    // ── 게시글 삭제 (S3 이미지 동기화 삭제)
    @Transactional
    public void delete(String email, Long attendanceId) {
        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (!attendance.getMember().getEmail().equals(email))
            throw new IllegalStateException("본인의 게시글만 삭제할 수 있습니다.");

        // 1. S3 이미지 먼저 삭제
        attendance.getImages()
                .forEach(img -> s3Uploader.deleteFile(img.getImageUrl()));

        // 2. DB 레코드 삭제 (CASCADE로 AttendanceImage도 함께)
        attendanceRepo.delete(attendance);
    }

    // ── 내 월별 인증 목록
    public List<AttendanceResponse> getMyMonthly(String email, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return attendanceRepo
                .findByMemberAndCreatedDateBetweenOrderByCreatedDateAsc(
                        find(email), ym.atDay(1), ym.atEndOfMonth())
                .stream().map(AttendanceResponse::new).toList();
    }

    // ── 내 월별 인증 횟수
    public long getMyCount(String email, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return attendanceRepo.countByMemberAndCreatedDateBetween(
                find(email), ym.atDay(1), ym.atEndOfMonth());
    }

    // ── 날짜별 전체 피드 (홈 화면 — null이면 전체 최신순)
    public List<AttendanceResponse> getFeed(LocalDate date) {
        List<Attendance> list = (date == null)
                ? attendanceRepo.findAllByOrderByCreatedAtDesc()
                : attendanceRepo.findByCreatedDateOrderByCreatedAtDesc(date);
        return list.stream()
                .map(a -> new AttendanceResponse(a, commentRepo.countByAttendance(a)))
                .toList();
    }

    // ── 리더: 월별 전체 목록
    public List<AttendanceResponse> getAllMonthly(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return attendanceRepo
                .findByCreatedDateBetweenOrderByCreatedAtDesc(ym.atDay(1), ym.atEndOfMonth())
                .stream().map(AttendanceResponse::new).toList();
    }

    private Member find(String email) {
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
    }
    public long getMyTotalCount(String email) {
        return attendanceRepo.countByMemberAndCreatedDateBetween(
                find(email),
                LocalDate.of(2000, 1, 1),
                LocalDate.now());
    }
    public long getMyYearCount(String email, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end   = LocalDate.of(year, 12, 31);
        return attendanceRepo.countByMemberAndCreatedDateBetween(
                find(email), start, end);
    }

    // ── 내 전체 인증 목록 (등록순)
    public List<AttendanceResponse> getMyAll(String email) {
        return attendanceRepo.findByMemberOrderByCreatedAtDesc(find(email))
                .stream().map(AttendanceResponse::new).toList();
    }
}
