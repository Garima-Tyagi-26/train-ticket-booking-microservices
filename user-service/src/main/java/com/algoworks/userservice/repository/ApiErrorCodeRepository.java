package com.algoworks.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algoworks.userservice.entity.ApiErrorCode;

public interface ApiErrorCodeRepository extends JpaRepository<ApiErrorCode, Long>{

	Optional<ApiErrorCode> findByErrorCodeAndActiveTrue(String errorCode); 
	// finds a registered code only when it is active
}
