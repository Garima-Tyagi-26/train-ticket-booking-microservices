package com.algoworks.trainticketbooking.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String email, String role) {

        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, Jwts.SIG.HS384)
                .compact();
    }
}
