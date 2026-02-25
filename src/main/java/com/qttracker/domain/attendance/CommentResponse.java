package com.qttracker.domain.attendance;

import lombok.Getter;

@Getter
public class CommentResponse {
    private Long   id;
    private String memberName;
    private String memberEmail;
    private String content;
    private String createdDate;

    public CommentResponse(Comment c) {
        this.id          = c.getId();
        this.memberName  = c.getMember().getName();
        this.memberEmail = c.getMember().getEmail();
        this.content     = c.getContent();
        this.createdDate = c.getCreatedAt().toLocalDate().toString();
    }
}
