package com.qttracker.domain.member;

import com.qttracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
                member.getRole().name(), member.getEmail());
    }

    // ── 비밀번호 찾기: 이메일 + 이름 확인 후 새 비밀번호로 즉시 변경
    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        Member member = memberRepository
                .findByEmailAndName(req.getEmail(), req.getName())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 계정을 찾을 수 없습니다."));
        member.changePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    // ── 마이페이지 비밀번호 변경 (로그인된 본인만)
    @Transactional
    public void changePassword(String email, PasswordChangeRequest req) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        if (!passwordEncoder.matches(req.getCurrentPassword(), member.getPassword()))
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        member.changePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    // ── FCM 토큰 저장
    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        member.updateFcmToken(fcmToken);
    }

    // ── FCM 토큰 삭제 (알림 끄기)
    @Transactional
    public void clearFcmToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        member.updateFcmToken(null);
    }

    // ── 관리자: 특정 멤버 비밀번호를 123456789로 초기화
    @Transactional
    public void resetPasswordByAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));
        member.changePassword(passwordEncoder.encode("123456789"));
    }
}
