package com.algoworks.authenticationservice.service;

import org.springframework.stereotype.Service;
import com.algoworks.authenticationservice.dto.RefreshTokenRequest;
import com.algoworks.authenticationservice.dto.TokenRequest;
import com.algoworks.authenticationservice.dto.TokenResponse;
import com.algoworks.authenticationservice.security.JwtService;
import com.algoworks.authenticationservice.entity.RefreshToken;
import com.algoworks.authenticationservice.repository.RefreshTokenRepository;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;
	
	
	public AuthenticationService(JwtService jwtService,
			RefreshTokenRepository refreshTokenRepository) {
		
		this.jwtService = jwtService;	
		this.refreshTokenRepository = refreshTokenRepository;
	}
	
	
    public String refreshAccessToken(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // 1. Hash the token and find its database record
        String tokenHash = jwtService.hashToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"));

        // 2. Check revocation and database expire
        if (storedToken.isRevoked()
            || !storedToken.getExpiresAt().isAfter(Instant.now())) {

                throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token is expired or revoked");
        }

        // 3. Generate a new access token using stored user details
        return jwtService.generateToken(
                storedToken.getUserId(),
                storedToken.getEmail(),
                storedToken.getRoles());
    }

	
	public TokenResponse generateTokens(TokenRequest request) {

    String accessToken = jwtService.generateToken(
                         request.getUserId(),
                         request.getEmail(),
                         request.getRole());

    String refreshToken = jwtService.generateRefreshToken();

    RefreshToken refreshTokenEntity = new RefreshToken();

    refreshTokenEntity.setTokenHash(jwtService.hashToken(refreshToken));
    refreshTokenEntity.setUserId(request.getUserId());
    refreshTokenEntity.setEmail(request.getEmail());
    refreshTokenEntity.setRoles(request.getRole());
    refreshTokenEntity.setExpiresAt(jwtService.getRefreshTokenExpiry());

    refreshTokenRepository.save(refreshTokenEntity);

    return new TokenResponse(accessToken, refreshToken);
}


}
