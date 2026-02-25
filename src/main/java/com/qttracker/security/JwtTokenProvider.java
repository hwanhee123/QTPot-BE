package com.qttracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key  key;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}")     String secret,
            @Value("${jwt.expiration}") long   expiration) {
        this.key        = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public String createToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmail(String token) {
        return claims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try { claims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
