package com.algoworks.trainticketbooking.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;

import io.jsonwebtoken.security.Keys;

@Configuration
public class SecurityConfig {

	@Value("${jwt.secret}")
	private String secretKey;
	 
	 @Bean
	 public PasswordEncoder passwordEncoder() {
		 return new BCryptPasswordEncoder();
	 }
	
	 
	 @Bean
	 public JwtDecoder jwtDecoder() {
	     SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

	     NimbusJwtDecoder decoder = NimbusJwtDecoder
	                                .withSecretKey(key)
	                                .build();

	     OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
	           JwtValidators.createDefault(),
	           jwt -> {
	             if ("access".equals(jwt.getClaimAsString("type"))) {
	                     return OAuth2TokenValidatorResult.success();
	             }

	             return OAuth2TokenValidatorResult.failure(new OAuth2Error(
	                                         "invalid_token",
	                                         "Access token required",
	                                         null));
	           });

	     decoder.setJwtValidator(validator);
	     return decoder;
	 }
	 
	 
	 @Bean
	 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())
	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers(
	                    "/swagger-ui/**",
	                    "/v3/api-docs/**"
	                ).permitAll()
	                
	                .requestMatchers(HttpMethod.POST, "/trains/**").hasRole("ADMIN")

	                .requestMatchers(HttpMethod.PUT, "/trains/**").hasRole("ADMIN")

	                .requestMatchers(HttpMethod.DELETE, "/trains/**").hasRole("ADMIN")

	                .anyRequest().authenticated()
	            )
	            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
	                                                              new JwtAuthenticationConverter())));
	         return http.build();
	    }
}
