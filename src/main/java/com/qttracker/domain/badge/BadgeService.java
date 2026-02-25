package com.qttracker.domain.badge;

import com.qttracker.domain.attendance.AttendanceRepository;
import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository      badgeRepo;
    private final AttendanceRepository attendanceRepo;
    private final MemberRepository     memberRepo;

    // 뱃지 등급 결정: 최고 등급 1개만 지급
    private String determineBadgeName(long count) {
        if (count >= 30) return "꽃";
        if (count >= 20) return "새싹";
        if (count >= 10) return "씨앗";
        return null;
    }

    // ── 매월 1일 00:00 자동 실행 — 전월 인증 횟수 기준으로 최고 등급 뱃지 지급
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void grantBadges() {
        YearMonth last     = YearMonth.now().minusMonths(1);
        String    monthStr = last.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate start    = last.atDay(1);
        LocalDate end      = last.atEndOfMonth();

        for (Member member : memberRepo.findAll()) {
            // 이미 해당 월 뱃지가 있으면 중복 지급 안 함
            if (badgeRepo.existsByMemberAndMonth(member, monthStr)) continue;

            long count = attendanceRepo
                    .countByMemberAndCreatedDateBetween(member, start, end);
            String badgeName = determineBadgeName(count);
            if (badgeName == null) continue;

            badgeRepo.save(Badge.builder()
                    .member(member)
                    .month(monthStr)
                    .badgeName(badgeName)
                    .build());
            log.info("[Badge] 지급: {} / {} / {}일 → {}", member.getName(), monthStr, count, badgeName);
        }
    }

    // ── 뱃지 조회 시 전월 미지급 체크 (스케줄러 보완용)
    @Transactional
    public List<BadgeResponse> getMyBadges(String email) {
        Member member = memberRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 전월 뱃지가 아직 없으면 지급 시도
        YearMonth last     = YearMonth.now().minusMonths(1);
        String    monthStr = last.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        if (!badgeRepo.existsByMemberAndMonth(member, monthStr)) {
            LocalDate start = last.atDay(1);
            LocalDate end   = last.atEndOfMonth();
            long count = attendanceRepo.countByMemberAndCreatedDateBetween(member, start, end);
            String badgeName = determineBadgeName(count);
            if (badgeName != null) {
                badgeRepo.save(Badge.builder()
                        .member(member)
                        .month(monthStr)
                        .badgeName(badgeName)
                        .build());
                log.info("[Badge] 보완 지급: {} / {} / {}일 → {}", member.getName(), monthStr, count, badgeName);
            }
        }

        return badgeRepo.findByMember(member)
                .stream().map(BadgeResponse::new).toList();
    }
}
