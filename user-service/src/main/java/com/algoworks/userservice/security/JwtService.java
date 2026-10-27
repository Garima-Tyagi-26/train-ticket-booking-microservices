package com.algoworks.userservice.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    
    // this method will accept the JWT only with access claim
    public boolean isTokenValid(String token) {
        try {
            
        	Claims claims = extractClaims(token);
            return "access".equals(claims.get("type", String.class));
 
        } catch (Exception e) {
            return false;
        }
    }
}