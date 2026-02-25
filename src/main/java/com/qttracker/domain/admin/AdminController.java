package com.qttracker.domain.admin;

import com.qttracker.domain.attendance.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── 전체 멤버 목록 (이름, 이메일, role, 이번달 인증, 누적)
    @GetMapping("/members")
    public ResponseEntity<List<MemberSummaryResponse>> members() {
        return ResponseEntity.ok(adminService.getAllMembers());
    }

    // ── 특정 멤버 비밀번호 123456789로 초기화
    @PostMapping("/members/{id}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable Long id) {
        adminService.resetMemberPassword(id);
        return ResponseEntity.ok("비밀번호가 초기화되었습니다.");
    }

    // ── 날짜별 인증 명단 + 사진
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponse>> attendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(adminService.getByDate(date));
    }
}
