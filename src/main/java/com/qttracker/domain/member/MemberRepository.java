package com.qttracker.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Member> findByEmailAndName(String email, String name);

    // 큐티 알림 스케줄러: 특정 시간에 알림 받을 유저 조회
    List<Member> findByQtNotiTimeAndFcmTokenIsNotNull(String qtNotiTime);
}
