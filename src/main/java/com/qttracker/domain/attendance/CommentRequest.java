package com.qttracker.domain.attendance;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentRequest {
    @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다.")
    private String content;
}
