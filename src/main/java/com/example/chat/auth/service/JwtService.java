package com.example.chat.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Fixed: Using the same secret, but we will decode it as BASE64URL
    private final String SECRET_BASE64 = "gQ_t1xL7jWd-qJ8yH_vA2F4kP0zB9iYxR5uS6cO3mE8wV7uZgYbXcDaHeFiGjKlNmOqPrS0t";

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    /**
     * Retrieves the signing key using BASE64URL decoder to handle the '-' and '_' characters.
     */
    private SecretKey getSigningKey() {
        // Changed from Decoders.BASE64 to Decoders.BASE64URL
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT token using JJWT 0.13.0 syntax.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey()) // Algorithm is automatically detected (HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Updated parser logic for JJWT 0.13.0.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}