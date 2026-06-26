package com.bizkredit.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Handles JWT creation, claim extraction, signature verification, and expiry validation
@Slf4j
@Component
public class JwtUtil {

    // Secret key used to sign and verify JWT tokens
    @Value("${jwt.secret}")
    private String secretKey;

    // Token validity duration in milliseconds
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Generate JWT without extra custom claims
    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails);
    }

    // Generate JWT with additional custom claims like userId, role, and branchId
    public String generateTokenWithClaims(UserDetails userDetails, Map<String, Object> extraClaims)
    {
        return buildToken(extraClaims, userDetails);
    }

    // Build JWT token with claims, subject, issue time, expiry time, and signature
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract email/username from JWT subject
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate token by checking username match and token expiry
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token); // from jwt oken
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Check whether JWT token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract token expiration date
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a specific claim from JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // Parse token, verify signature, and return all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Convert configured secret string into signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}