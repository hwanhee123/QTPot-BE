package com.qttracker.domain.badge;

import com.qttracker.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    boolean existsByMemberAndMonth(Member member, String month);
    List<Badge> findByMember(Member member);
}
