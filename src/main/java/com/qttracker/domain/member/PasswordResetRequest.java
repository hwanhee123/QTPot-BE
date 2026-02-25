package com.qttracker.domain.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetRequest {
    @NotBlank private String email;
    @NotBlank private String name;
    @NotBlank private String newPassword;
}
