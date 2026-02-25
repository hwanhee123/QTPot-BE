package com.qttracker.domain.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── 다중 사진 업로드 (images: List<MultipartFile>)
    @PostMapping
    public ResponseEntity<AttendanceResponse> upload(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "content", defaultValue = "") String content,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "isPrivate", defaultValue = "false") boolean isPrivate) {
        return ResponseEntity.ok(
                attendanceService.upload(ud.getUsername(), images, content, date, isPrivate));
    }

    // ── 소감 수정
    @PatchMapping("/{id}/content")
    public ResponseEntity<AttendanceResponse> updateContent(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id,
            @RequestBody ContentUpdateRequest req) {
        return ResponseEntity.ok(
                attendanceService.updateContent(ud.getUsername(), id, req));
    }

    // ── 삭제 (S3 동기화)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id) {
        attendanceService.delete(ud.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // ── 홈 피드: 전체 최신 / 날짜별 필터
    @GetMapping("/feed")
    public ResponseEntity<List<AttendanceResponse>> feed(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getFeed(date));
    }

    // ── 내 월별 목록
    @GetMapping("/my")
    public ResponseEntity<List<AttendanceResponse>> myList(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        int y = year==0  ? LocalDate.now().getYear()       : year;
        int m = month==0 ? LocalDate.now().getMonthValue() : month;
        return ResponseEntity.ok(attendanceService.getMyMonthly(ud.getUsername(), y, m));
    }

    // ── 내 월별 횟수
    @GetMapping("/my/count")
    public ResponseEntity<Long> myCount(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        int y = year == 0 ? LocalDate.now().getYear() : year;
        if (month == 0) {
            return ResponseEntity.ok(
                    attendanceService.getMyYearCount(ud.getUsername(), y));
        }
        return ResponseEntity.ok(
                attendanceService.getMyCount(ud.getUsername(), y, month));
    }

    // ── 리더: 월별 전체
    @GetMapping("/all")
    public ResponseEntity<List<AttendanceResponse>> allList(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        int y = year==0  ? LocalDate.now().getYear()       : year;
        int m = month==0 ? LocalDate.now().getMonthValue() : month;
        return ResponseEntity.ok(attendanceService.getAllMonthly(y, m));
    }

    @GetMapping("/my/total-count")
    public ResponseEntity<Long> myTotalCount(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(
                attendanceService.getMyTotalCount(ud.getUsername()));
    }

    // ── 내 전체 인증 목록 (등록순)
    @GetMapping("/my/all")
    public ResponseEntity<List<AttendanceResponse>> myAll(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(attendanceService.getMyAll(ud.getUsername()));
    }

}
