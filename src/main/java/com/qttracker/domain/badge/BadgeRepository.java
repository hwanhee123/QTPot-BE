package com.qttracker.domain.badge;

import com.qttracker.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    boolean existsByMemberAndMonth(Member member, String month);
    Optional<Badge> findByMemberAndMonth(Member member, String month);
    List<Badge> findByMember(Member member);
}
