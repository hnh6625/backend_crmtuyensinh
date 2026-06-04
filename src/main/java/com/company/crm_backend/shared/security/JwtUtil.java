package com.company.crm_backend.shared.security;

import com.company.crm_backend.User.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry}")
    private long accessExpiry;   // giây

    @Getter
    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshExpiry;  // giây

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().getRoleName())
                .claim("fullName", user.getFullName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiry * 1000))
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiry * 1000))
                .signWith(key())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
