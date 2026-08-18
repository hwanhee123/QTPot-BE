package com.qttracker.domain.attendance;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ContentUpdateRequest {
    @Size(max = 2000, message = "소감은 2000자 이하여야 합니다.")
    private String content;
}
