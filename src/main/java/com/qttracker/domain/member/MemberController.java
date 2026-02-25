package com.qttracker.domain.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ── 회원가입
    @PostMapping("/api/auth/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest req) {
        memberService.signup(req);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // ── 로그인
    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(memberService.login(req));
    }

    // ── 비밀번호 찾기 (비로그인 상태에서 이메일+이름 확인 후 변경)
    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody PasswordResetRequest req) {
        memberService.resetPassword(req);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // ── 마이페이지 비밀번호 변경 (로그인 필요)
    @PutMapping("/api/members/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody PasswordChangeRequest req) {
        memberService.changePassword(ud.getUsername(), req);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // ── FCM 토큰 저장
    @PutMapping("/api/members/me/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody FcmTokenRequest req) {
        memberService.updateFcmToken(ud.getUsername(), req.getFcmToken());
        return ResponseEntity.ok("FCM 토큰이 저장되었습니다.");
    }

    // ── FCM 토큰 삭제 (알림 끄기)
    @DeleteMapping("/api/members/me/fcm-token")
    public ResponseEntity<String> clearFcmToken(
            @AuthenticationPrincipal UserDetails ud) {
        memberService.clearFcmToken(ud.getUsername());
        return ResponseEntity.ok("알림이 꺼졌습니다.");
    }
}
