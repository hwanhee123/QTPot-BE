package com.qttracker.domain.badge;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingResponse>> ranking(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        int y = year  == 0 ? LocalDate.now().getYear()       : year;
        int m = month == 0 ? LocalDate.now().getMonthValue() : month;
        return ResponseEntity.ok(rankingService.getRanking(y, m));
    }
}
