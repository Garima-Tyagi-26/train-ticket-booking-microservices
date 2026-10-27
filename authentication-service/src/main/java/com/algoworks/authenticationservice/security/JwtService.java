package com.algoworks.authenticationservice.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiration,
                      @Value("${jwt.refresh-expiration}") long refreshExpiration) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(Long userId, String email, String role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    
    //generates a random string with 256 bits of randomness
    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    
    public String getEmailFromToken(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    
    public boolean isAccessToken(String token) {
        try {
            var claims = Jwts.parser()
                         .verifyWith(secretKey)
                         .build()
                         .parseSignedClaims(token)
                         .getPayload();

            // any signed JWT without any access type wouldn't be accepted for protected API  
            return "access".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
    
    
    public String hashToken(String token) {
         try {
               MessageDigest digest = MessageDigest.getInstance("SHA-256");

               byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

               StringBuilder hex = new StringBuilder();

               for (byte b : hash) {
                  hex.append(String.format("%02x", b & 0xff));
               }

               return hex.toString();

               } catch (NoSuchAlgorithmException e) {
                   throw new IllegalStateException("SHA-256 is unavailable", e);
               }
     }

    
     public Instant getRefreshTokenExpiry() {
            return Instant.now().plusMillis(refreshExpiration);
     }

}