package com.mkrasikoff.epigraph.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private static final long RESET_EXPIRATION_MS = 30 * 60 * 1000L; // 30 minutes

    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a short-lived password-reset token.
     * The token carries the user's ID as subject and a "purpose" claim
     * so it can be distinguished from a regular session token.
     */
    public String generateResetToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("purpose", "password-reset")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + RESET_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Returns true only when the token is valid AND carries purpose=password-reset.
     */
    public boolean isResetTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return "password-reset".equals(claims.get("purpose", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
