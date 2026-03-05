package com.qttracker.domain.attendance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LikeResponse {
    private long likeCount;
    private boolean isLiked;
}
