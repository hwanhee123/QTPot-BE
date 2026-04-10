package com.qttracker.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSettingResponse {
    private boolean commentNotiEnabled;
    private String qtNotiTime; // "HH:mm" 또는 null
}
