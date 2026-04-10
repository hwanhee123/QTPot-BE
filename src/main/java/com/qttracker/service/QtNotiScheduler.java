package com.qttracker.service;

import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QtNotiScheduler {

    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 매 분마다 실행: 현재 시각(HH:mm)과 qtNotiTime이 일치하는 유저에게 FCM 발송
    @Scheduled(cron = "0 * * * * *")
    public void sendQtNotifications() {
        String currentTime = LocalTime.now().format(FORMATTER);
        List<Member> targets = memberRepository.findByQtNotiTimeAndFcmTokenIsNotNull(currentTime);

        if (targets.isEmpty()) return;

        log.info("큐티 알림 발송: {}시 대상 {}명", currentTime, targets.size());

        for (Member member : targets) {
            fcmService.sendPush(
                    member.getFcmToken(),
                    "📖 큐티할 시간이에요!",
                    member.getName() + "님, 오늘의 큐티를 시작해보세요 🙏"
            );
        }
    }
}
