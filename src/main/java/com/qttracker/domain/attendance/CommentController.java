package com.qttracker.domain.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/{attendanceId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ── 댓글 목록 조회
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long attendanceId) {
        return ResponseEntity.ok(commentService.getComments(attendanceId));
    }

    // ── 댓글 작성
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long attendanceId,
            @RequestBody CommentRequest req) {
        return ResponseEntity.ok(
                commentService.addComment(ud.getUsername(), attendanceId, req));
    }

    // ── 댓글 삭제 (본인만)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long attendanceId,
            @PathVariable Long commentId) {
        commentService.deleteComment(ud.getUsername(), attendanceId, commentId);
        return ResponseEntity.noContent().build();
    }
}
