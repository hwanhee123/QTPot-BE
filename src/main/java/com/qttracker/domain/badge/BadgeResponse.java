package com.qttracker.domain.badge;

import lombok.Getter;

@Getter
public class BadgeResponse {
    private Long   id;
    private String month;
    private String badgeName;

    public BadgeResponse(Badge b) {
        this.id        = b.getId();
        this.month     = b.getMonth();
        this.badgeName = b.getBadgeName();
    }
}
