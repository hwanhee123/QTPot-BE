package com.qttracker.config;

import com.qttracker.security.CustomUserDetailsService;
import com.qttracker.security.JwtAuthenticationFilter;
import com.qttracker.security.JwtTokenProvider;
import com.qttracker.security.RateLimitFilter;
import com.qttracker.security.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider        jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RevokedTokenRepository  revokedTokenRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(c -> c.configurationSource(corsSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // formLogin/httpBasic을 안 쓰는 순수 JWT 구성이라, entry point를 직접 안 정하면
                // Spring Security가 기본값(Http403ForbiddenEntryPoint)으로 떨어져서 토큰이
                // 없거나 무효/만료된 요청까지 전부 403으로 응답함 — FE는 401만 자동 로그아웃
                // 처리하므로 이 기본값 때문에 만료된 세션이 자동 로그아웃 없이 계속 403만 받는
                // 상태로 막혀있었음. 인증 자체가 안 된 경우(401)와 인증은 됐지만 권한이 부족한
                // 경우(403)를 명시적으로 분리 — 이 프로젝트의 Spring Security 버전에서는 entry
                // point만 설정하면 권한부족(hasRole 불일치)까지 401로 새는 걸 실측으로 확인해서,
                // accessDeniedHandler도 같이 명시해 403이 유지되도록 함.
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다."))
                        .accessDeniedHandler((req, res, ex) ->
                                writeJsonError(res, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.")))
                .authorizeHttpRequests(auth -> auth
                        // sendError()가 /error로 forward하는데, 이게 보안 필터를 다시 타면
                        // OncePerRequestFilter인 JwtAuthenticationFilter는 error dispatch에서
                        // 기본적으로 스킵되어(익명 처리) 원래 상태코드(예: 403)를 401로 덮어써버림
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()       // 회원가입, 로그인
                        .requestMatchers("/api/attendance/all").hasRole("LEADER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 관리자 전용
                        .anyRequest().authenticated())

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, revokedTokenRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimitFilter(), JwtAuthenticationFilter.class)
                .build();
    }

    // GlobalExceptionHandler와 동일한 {"message": "..."} 형식 — 이 필터 체인 레벨 에러는
    // @RestControllerAdvice를 안 타서 여기서 직접 맞춰줘야 FE 응답 형식이 일관됨
    private void writeJsonError(HttpServletResponse res, int status, String message) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://3.35.27.209", "https://qtpot.kro.kr"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
