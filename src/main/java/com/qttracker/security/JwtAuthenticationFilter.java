package com.qttracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider  jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  req,
                                    HttpServletResponse res,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String token = resolve(req);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                UserDetails user = userDetailsService
                        .loadUserByUsername(jwtTokenProvider.getEmail(token));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities()));
            } catch (Exception ignored) {
                // 토큰이 유효해도 사용자를 찾을 수 없으면 인증 없이 진행
            }
        }
        chain.doFilter(req, res);
    }

    private String resolve(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? h.substring(7) : null;
    }
}
