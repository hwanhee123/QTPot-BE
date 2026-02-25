package com.qttracker.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    // 비밀번호 찾기: 이메일 + 이름으로 본인 확인
    Optional<Member> findByEmailAndName(String email, String name);
}
