package com.qttracker.domain.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FcmTokenRequest {

    @NotBlank
    private String fcmToken;
}
