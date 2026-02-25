package com.qttracker.domain.badge;

import com.qttracker.domain.attendance.AttendanceRepository;
import com.qttracker.domain.member.Member;
import com.qttracker.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final AttendanceRepository attendanceRepo;
    private final MemberRepository     memberRepo;

    public List<RankingResponse> getRanking(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);

        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : attendanceRepo
                .countGroupByMember(ym.atDay(1), ym.atEndOfMonth())) {
            if (row[0] == null || row[1] == null) continue;
            countMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        Map<Long, String> nameMap = memberRepo.findAll().stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));

        List<Map.Entry<Long, Long>> sorted = countMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .toList();

        List<RankingResponse> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Long id = sorted.get(i).getKey();
            result.add(new RankingResponse(
                    i + 1,
                    nameMap.getOrDefault(id, "알 수 없음"),
                    sorted.get(i).getValue()));
        }
        return result;
    }
}
