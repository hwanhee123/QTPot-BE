package com.qttracker.domain.attendance;

import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import com.qttracker.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository    commentRepo;
    private final AttendanceRepository attendanceRepo;
    private final MemberRepository     memberRepo;
    private final FcmService           fcmService;

    public List<CommentResponse> getComments(Long attendanceId) {
        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return commentRepo.findByAttendanceOrderByCreatedAtAsc(attendance)
                .stream().map(CommentResponse::new).toList();
    }

    @Transactional
    public CommentResponse addComment(String email, Long attendanceId, CommentRequest req) {
        if (req.getContent() == null || req.getContent().isBlank())
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        Member member = memberRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        Comment comment = commentRepo.save(Comment.builder()
                .attendance(attendance)
                .member(member)
                .content(req.getContent())
                .build());

        Member postOwner = attendance.getMember();
        if (!postOwner.getEmail().equals(email)) {
            fcmService.sendPush(
                    postOwner.getFcmToken(),
                    member.getName() + "님이 댓글을 달았습니다.",
                    req.getContent()
            );
        }

        return new CommentResponse(comment);
    }

    @Transactional
    public void deleteComment(String email, Long attendanceId, Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!comment.getMember().getEmail().equals(email))
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        commentRepo.delete(comment);
    }
}
