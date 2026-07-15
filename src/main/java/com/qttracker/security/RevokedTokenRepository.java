package com.qttracker.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
