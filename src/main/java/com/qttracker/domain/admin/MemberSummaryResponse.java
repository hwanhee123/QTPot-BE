package com.qttracker.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberSummaryResponse {
    private Long   id;
    private String name;
    private String email;
    private String role;
    private long   thisMonthCount;  // 이번 달
    private long   yearCount;       // 올해
    private long   totalCount;      // 전체 누적
}
