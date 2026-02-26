package com.qttracker.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendPush(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isBlank()) return;

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                // 웹 푸시: 아이콘 지정 (FCM이 자동 표시 → 서비스워커 중복 표시 방지)
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setIcon("/pwa-192x192.png")
                                .build())
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: token={}", fcmToken.substring(0, 20) + "...");
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패: {} / token={}", e.getMessage(), fcmToken.substring(0, 20) + "...");
        }
    }
}
