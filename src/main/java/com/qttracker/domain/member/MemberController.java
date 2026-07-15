package com.qttracker.domain.member;

import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/api/auth/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest req) {
        memberService.signup(req);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(memberService.login(req));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            memberService.logout(header.substring(7));
        }
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    @PutMapping("/api/members/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody PasswordChangeRequest req) {
        memberService.changePassword(ud.getUsername(), req);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    @PutMapping("/api/members/me/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody FcmTokenRequest req) {
        memberService.updateFcmToken(ud.getUsername(), req.getFcmToken());
        return ResponseEntity.ok("FCM 토큰이 저장되었습니다.");
    }

    @DeleteMapping("/api/members/me/fcm-token")
    public ResponseEntity<String> clearFcmToken(
            @AuthenticationPrincipal UserDetails ud) {
        memberService.clearFcmToken(ud.getUsername());
        return ResponseEntity.ok("알림이 꺼졌습니다.");
    }

    // ── 알림 설정 조회
    @GetMapping("/api/members/me/notification-settings")
    public ResponseEntity<NotificationSettingResponse> getNotificationSettings(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(memberService.getNotificationSettings(ud.getUsername()));
    }

    // ── 알림 설정 저장
    @PutMapping("/api/members/me/notification-settings")
    public ResponseEntity<String> updateNotificationSettings(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody NotificationSettingRequest req) {
        memberService.updateNotificationSettings(ud.getUsername(), req);
        return ResponseEntity.ok("알림 설정이 저장되었습니다.");
    }
}
