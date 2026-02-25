package com.qttracker.domain.admin;

import com.qttracker.domain.attendance.AttendanceRepository;
import com.qttracker.domain.attendance.AttendanceResponse;
import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import com.qttracker.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository     memberRepo;
    private final AttendanceRepository attendanceRepo;
    private final MemberService        memberService;

    public List<MemberSummaryResponse> getAllMembers() {
        YearMonth ym       = YearMonth.now();
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();
        // 올해 1월 1일 ~ 오늘
        LocalDate yearStart  = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate today      = LocalDate.now();

        return memberRepo.findAll().stream().map(m -> {
            long thisMonth = attendanceRepo
                    .countByMemberAndCreatedDateBetween(m, monthStart, monthEnd);
            long yearCount = attendanceRepo
                    .countByMemberAndCreatedDateBetween(m, yearStart, today);
            long total = attendanceRepo
                    .countByMemberAndCreatedDateBetween(m,
                            LocalDate.of(2000, 1, 1), today);
            return new MemberSummaryResponse(
                    m.getId(), m.getName(), m.getEmail(),
                    m.getRole().name(), thisMonth, yearCount, total);
        }).toList();
    }

    public void resetMemberPassword(Long memberId) {
        memberService.resetPasswordByAdmin(memberId);
    }

    public List<AttendanceResponse> getByDate(LocalDate date) {
        return attendanceRepo
                .findByCreatedDateOrderByCreatedAtDesc(date)
                .stream().map(AttendanceResponse::new).toList();
    }
}
