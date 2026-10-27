package com.algoworks.userservice.client;

import com.algoworks.userservice.dto.TokenRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthServiceClient {

    private final RestClient restClient;
    private final String internalApiSecret;

    
    public AuthServiceClient(@Value("${internal.api.secret}") String internalApiSecret,
    		                 @Value("${auth-service.base-url}") String authServiceBaseUrl) {

    	this.restClient = RestClient.builder().baseUrl(authServiceBaseUrl).build();
        this.internalApiSecret = internalApiSecret;
    }

    
    /*
     * Requests access and refresh tokens from the Authentication Service.
     * @param request the user details required to generate the tokens
     * @return the generated access and refresh tokens
     */
    
    
    public TokenResponse generateTokens(TokenRequest request) {

        return restClient.post()
                .uri("/auth/token")
                .header("X-Internal-Secret", internalApiSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TokenResponse.class);
    }

    
    public static class TokenResponse {

        private String accessToken;
        private String refreshToken;

        public TokenResponse() {
        }

        
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
