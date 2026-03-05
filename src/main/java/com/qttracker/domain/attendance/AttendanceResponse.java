package com.qttracker.domain.attendance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
public class AttendanceResponse {
    private Long         id;
    private List<String> imageUrls;
    private String       content;
    @JsonProperty("isPrivate")
    private boolean      isPrivate;
    private LocalDate    createdDate;
    private String       memberName;
    private String       memberEmail;
    private long         commentCount;
    private long         likeCount;
    @JsonProperty("isLiked")
    private boolean      isLiked;

    public AttendanceResponse(Attendance a) {
        this(a, 0L);
    }

    public AttendanceResponse(Attendance a, long commentCount) {
        this(a, commentCount, 0L, false);
    }

    public AttendanceResponse(Attendance a, long commentCount, long likeCount, boolean isLiked) {
        this.id           = a.getId();
        this.imageUrls    = a.getImages().stream()
                .map(AttendanceImage::getImageUrl).toList();
        this.content      = a.getContent();
        this.isPrivate    = a.isPrivate();
        this.createdDate  = a.getCreatedDate();
        this.memberName   = a.getMember().getName();
        this.memberEmail  = a.getMember().getEmail();
        this.commentCount = commentCount;
        this.likeCount    = likeCount;
        this.isLiked      = isLiked;
    }
}
