package com.qttracker.domain.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    @NotBlank private String currentPassword;
    @NotBlank private String newPassword;
}
