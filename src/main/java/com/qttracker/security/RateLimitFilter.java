package com.qttracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * /api/auth/login, /api/auth/signup에 대한 단순 인메모리 IP별 rate limit.
 * 소규모 단일 인스턴스 배포 전제(다중 인스턴스면 공유 저장소로 교체 필요).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long LOGIN_WINDOW_MS  = Duration.ofMinutes(5).toMillis();
    private static final int  LOGIN_MAX        = 10;
    private static final long SIGNUP_WINDOW_MS = Duration.ofMinutes(10).toMillis();
    private static final int  SIGNUP_MAX       = 5;

    private final Map<String, ConcurrentLinkedDeque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        boolean isLogin  = "POST".equalsIgnoreCase(req.getMethod()) && path.equals("/api/auth/login");
        boolean isSignup = "POST".equalsIgnoreCase(req.getMethod()) && path.equals("/api/auth/signup");

        if (!isLogin && !isSignup) {
            chain.doFilter(req, res);
            return;
        }

        long windowMs = isLogin ? LOGIN_WINDOW_MS : SIGNUP_WINDOW_MS;
        int  max      = isLogin ? LOGIN_MAX : SIGNUP_MAX;
        String key = clientIp(req) + "|" + path;

        ConcurrentLinkedDeque<Long> timestamps = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= max) {
                res.setStatus(429);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
            timestamps.addLast(now);
        }

        chain.doFilter(req, res);
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
