package com.ecommerce.cartservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Extract username (email) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token
    public Boolean validateToken(String token) {
        try {
//            return !isTokenExpired(token);
            Claims claims = extractAllClaims(token);
            boolean notExpired = !isTokenExpired(token);

            log.info("Token validation - Expired: {}", !notExpired);
            log.info("Token validation - Subject: {}", claims.getSubject());
            log.info("Token validation - Role: {}", claims.get("role"));
            log.info("Token validation - Issued: {}", claims.getIssuedAt());
            log.info("Token validation - Expires: {}", claims.getExpiration());

            return notExpired;
        } catch (Exception e) {
            log.error("Token validation exception: {}", e.getClass().getSimpleName());
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // Get signing key
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
