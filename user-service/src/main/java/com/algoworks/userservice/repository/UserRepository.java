package com.algoworks.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algoworks.userservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email); //Does a user with this email exist?
	
	Optional<User> findByEmail(String email); //the actual User whose email is this.
	
	Optional<User> findByResetPasswordToken(String token); //after OTP verification
}
