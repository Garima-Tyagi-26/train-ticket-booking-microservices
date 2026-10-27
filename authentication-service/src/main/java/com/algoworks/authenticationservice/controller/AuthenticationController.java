package com.algoworks.authenticationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algoworks.authenticationservice.dto.RefreshTokenRequest;
import com.algoworks.authenticationservice.dto.TokenRequest;
import com.algoworks.authenticationservice.dto.TokenResponse;
import com.algoworks.authenticationservice.service.AuthenticationService;

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Value("${internal.api.secret}")
	private String internalApiSecret;

	private final AuthenticationService authenticationService;
	
	
	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateTokens(@Valid @RequestBody TokenRequest request,
        @RequestHeader("X-Internal-Secret") String suppliedSecret) {

          boolean validSecret = MessageDigest.isEqual(internalApiSecret.getBytes(StandardCharsets.UTF_8),
                                 suppliedSecret.getBytes(StandardCharsets.UTF_8));

          if (!validSecret) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized service request");
          }

          return ResponseEntity.ok(authenticationService.generateTokens(request));
    }


    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {

         String accessToken =authenticationService.refreshAccessToken(request);
         
         return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }
 
}
