package com.qttracker.domain.member;

import com.qttracker.security.JwtTokenProvider;
import com.qttracker.security.RevokedToken;
import com.qttracker.security.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MemberService {

    // 관리자 초기화 시 부여하는 고정 임시 비밀번호 (다음 로그인 시 변경 강제)
    private static final String TEMP_PASSWORD = "123456789";

    private final MemberRepository        memberRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtTokenProvider        jwtTokenProvider;
    private final RevokedTokenRepository  revokedTokenRepository;

    @Transactional
    public void signup(SignupRequest req) {
        if (memberRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        memberRepository.save(Member.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role(Member.Role.USER)
                .build());
    }

    public LoginResponse login(LoginRequest req) {
        Member member = memberRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.getPassword(), member.getPassword()))
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
        return new LoginResponse(token, member.getName(),
                member.getRole().name(), member.getEmail(), member.isMustChangePassword());
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest req) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        if (!passwordEncoder.matches(req.getCurrentPassword(), member.getPassword()))
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        member.changePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        member.updateFcmToken(fcmToken);
    }

    @Transactional
    public void clearFcmToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        member.updateFcmToken(null);
    }

    // ── 알림 설정 조회
    public NotificationSettingResponse getNotificationSettings(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return new NotificationSettingResponse(
                member.isCommentNotiEnabled(),
                member.getQtNotiTime()
        );
    }

    // ── 알림 설정 저장
    @Transactional
    public void updateNotificationSettings(String email, NotificationSettingRequest req) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // qtNotiTime 형식 검증 (HH:mm 또는 null)
        String time = req.getQtNotiTime();
        if (time != null && !time.isBlank()) {
            if (!time.matches("^([01]\\d|2[0-3]):[0-5]\\d$"))
                throw new IllegalArgumentException("시간 형식이 올바르지 않습니다. (HH:mm)");
        } else {
            time = null;
        }

        member.updateNotificationSettings(req.isCommentNotiEnabled(), time);
    }

    @Transactional
    public void resetPasswordByAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));
        member.resetToTempPassword(passwordEncoder.encode(TEMP_PASSWORD));
    }

    // 현재 토큰을 블랙리스트에 등록해 만료 전이라도 재사용을 막는다.
    @Transactional
    public void logout(String token) {
        String jti = jwtTokenProvider.getJti(token);
        if (jti == null || revokedTokenRepository.existsByJti(jti)) return;
        LocalDateTime expiresAt = jwtTokenProvider.getExpiration(token)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        revokedTokenRepository.save(RevokedToken.builder()
                .jti(jti).expiresAt(expiresAt).build());
    }
}
