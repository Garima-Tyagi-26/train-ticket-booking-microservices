package com.algoworks.userservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.Value;

import com.algoworks.userservice.entity.ApiErrorCode;
import com.algoworks.userservice.repository.ApiErrorCodeRepository;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@Value("${app.system-code}")
	private String systemCode;

	@Value("${app.service-code}")
	private String serviceCode;

    private final ApiErrorCodeRepository apiErrorCodeRepository;

    public GlobalExceptionHandler(ApiErrorCodeRepository apiErrorCodeRepository) {
        this.apiErrorCodeRepository = apiErrorCodeRepository;
    }

    // Returns the registered code for invalid request data.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {

        return buildErrorResponse("002");
    }

    
    // Returns the registered code when an email already exists.
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {

        return buildErrorResponse("001");
    }
    
    
    // Returns the registered code when User does not exists.
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {

        return buildErrorResponse("003");
    }
    
    
    //Returns the registered code when email or password is invalid.
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {

        return buildErrorResponse("004");
    }

    
    private ResponseEntity<Map<String, String>> buildErrorResponse(String errorNumber) {
    	
        String errorCode = systemCode + "-" + serviceCode + "-" + errorNumber;

        // look up the registered code in the DB table
        ApiErrorCode error = apiErrorCodeRepository
                .findByErrorCodeAndActiveTrue(errorCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Required API error code is not configured: " + errorCode));

        // table stores the HTTP status as a number, so, HttpStatus.valueOf(409) converts it into Spring’s HttpStatus.CONFLICT.
        HttpStatus status = HttpStatus.valueOf(error.getHttpStatus());

        return ResponseEntity.status(status).body(Map.of("code", error.getErrorCode()));
    }
}
