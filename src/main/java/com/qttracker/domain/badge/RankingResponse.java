package com.qttracker.domain.badge;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankingResponse {
    private int    rank;
    private String name;
    private long   count;
}
