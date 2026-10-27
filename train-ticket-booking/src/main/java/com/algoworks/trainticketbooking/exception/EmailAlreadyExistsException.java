package com.algoworks.trainticketbooking.exception;

public class EmailAlreadyExistsException extends RuntimeException{

	public EmailAlreadyExistsException(String message) {
		super(message);
	}
}
