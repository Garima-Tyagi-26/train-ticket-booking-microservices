package com.algoworks.trainticketbooking.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<?> handleEmailAlreadyExists(EmailAlreadyExistsException ex){
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
	                                                          "Unable to process registration request"));
	}
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationErrors(
	        MethodArgumentNotValidException ex) {

	    Map<String, String> errors = new HashMap<>();

	    ex.getBindingResult().getFieldErrors().forEach(error ->
	            errors.put(error.getField(), error.getDefaultMessage())
	    );

	    return ResponseEntity.badRequest().body(errors);
	}
	
	
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException ex) {

	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	            .body(Map.of("message", ex.getMessage()));
	}
	
}