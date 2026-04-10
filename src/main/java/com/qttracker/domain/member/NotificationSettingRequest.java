package com.qttracker.domain.member;

import lombok.Getter;

@Getter
public class NotificationSettingRequest {
    private boolean commentNotiEnabled;
    private String qtNotiTime; // "HH:mm" 또는 null (알림 끄기)
}
