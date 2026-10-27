package com.algoworks.userservice.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.algoworks.userservice.dto.ChangePasswordRequest;
import com.algoworks.userservice.dto.ForgotPasswordRequest;
import com.algoworks.userservice.dto.RegisterRequest;
import com.algoworks.userservice.dto.ResetPasswordRequest;
import com.algoworks.userservice.dto.UpdateUserRequest;
import com.algoworks.userservice.dto.UserAuthResponse;
import com.algoworks.userservice.dto.UserResponse;
import com.algoworks.userservice.dto.VerifyOtpRequest;
import com.algoworks.userservice.entity.Role;
import com.algoworks.userservice.entity.User;
import com.algoworks.userservice.exception.EmailAlreadyExistsException;
import com.algoworks.userservice.exception.InvalidCredentialsException;
import com.algoworks.userservice.exception.UserNotFoundException;
import com.algoworks.userservice.repository.UserRepository;

import com.algoworks.userservice.client.AuthServiceClient;
import com.algoworks.userservice.dto.TokenRequest;

@Service
public class UserService {
   
	private final AuthServiceClient authServiceClient;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			           AuthServiceClient authServiceClient, EmailService emailService) {
		
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authServiceClient = authServiceClient;
		this.emailService = emailService;
	}
	
	
	// Register a new user
	public UserResponse registerUser(RegisterRequest request) {

	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new EmailAlreadyExistsException("Unable to process registration request");
	    }

	    User user = new User();

	    user.setName(request.getName());
	    user.setEmail(request.getEmail());
	    user.setPassword(passwordEncoder.encode(request.getPassword()));
	    user.setPhone(request.getPhone());
	    user.setRole(Role.USER);

	    User savedUser = userRepository.save(user);

	    UserResponse response = new UserResponse();

	    response.setId(savedUser.getId());
	    response.setName(savedUser.getName());
	    response.setEmail(savedUser.getEmail());
	    response.setPhone(savedUser.getPhone());

	    return response;
	}
	
	
	// get the user by his Id
	public UserResponse getUserById(Long id) {

	    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

	    UserResponse response = new UserResponse();

	    response.setId(user.getId());
	    response.setName(user.getName());
	    response.setEmail(user.getEmail());
	    response.setPhone(user.getPhone());

	    return response;
	}
	
	
	// update the details of existing user
	public UserResponse updateUser(Long id, UpdateUserRequest request) {

	    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

	    // Check if the new email is already used by another user
	    if (request.getEmail() != null
	            && !request.getEmail().equals(user.getEmail())
	            && userRepository.existsByEmail(request.getEmail())) {

	        throw new EmailAlreadyExistsException("Unable to process update request");
	    }

	    if (request.getName() != null) {
	        user.setName(request.getName());
	    }

	    if (request.getEmail() != null) {
	        user.setEmail(request.getEmail());
	    }

	    if (request.getPhone() != null) {
	        user.setPhone(request.getPhone());
	    }

	    User updatedUser = userRepository.save(user);

	    UserResponse response = new UserResponse();

	    response.setId(updatedUser.getId());
	    response.setName(updatedUser.getName());
	    response.setEmail(updatedUser.getEmail());
	    response.setPhone(updatedUser.getPhone());

	    return response;
	}
	
	
	// get the details of the logged-in user
	public UserResponse getMyProfile() {

	    String email = SecurityContextHolder.getContext().getAuthentication().getName();

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UserNotFoundException("User not found"));

	    UserResponse response = new UserResponse();
	    response.setId(user.getId());
	    response.setName(user.getName());
	    response.setEmail(user.getEmail());
	    response.setPhone(user.getPhone());

	    return response;
	}
	
	
	//change password method for the logged-in user
	public void changePassword(ChangePasswordRequest request) {

	    String email = SecurityContextHolder.getContext().getAuthentication().getName();

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UserNotFoundException("User not found"));

	    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

	        throw new InvalidCredentialsException("Current password is incorrect");
	    }

	    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
	}
	
	
    // forgot password method for existing user who is not logged-in
    public void forgotPassword(ForgotPasswordRequest request) {

       Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

       // Do not reveal whether the email exists
       if (userOptional.isEmpty()) {
           return;
       }

       User user = userOptional.get();

       // Generate 6-digit OTP
       String otp = String.format("%06d", new SecureRandom().nextInt(1000000));

       // Store only the hashed OTP
       String hashedOtp = passwordEncoder.encode(otp);

       user.setResetPasswordOtp(hashedOtp);
       user.setResetPasswordOtpExpiry(LocalDateTime.now().plusMinutes(5));
       user.setResetPasswordOtpAttempts(0);

       userRepository.save(user);

       try {
           // Send the original OTP to the user's email
           emailService.sendOtpEmail(user.getEmail(), otp);

       } catch (IOException ex) {
           // Do not expose technical details to the client
           throw new RuntimeException("Unable to send password reset email", ex);
    }
}


	public String verifyOtp(VerifyOtpRequest request) {

	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new RuntimeException("Invalid OTP request"));

	    // Check whether OTP exists
	    if (user.getResetPasswordOtp() == null) {
	        throw new RuntimeException("Invalid or expired OTP");
	    }

	    // Check maximum attempts
	    if (user.getResetPasswordOtpAttempts() >= 3) {
	        throw new RuntimeException("OTP verification attempts exceeded");
	    }

	    // Check OTP expire?
	    if (user.getResetPasswordOtpExpiry() == null
	            || user.getResetPasswordOtpExpiry().isBefore(LocalDateTime.now())) {

	        user.setResetPasswordOtp(null);
	        user.setResetPasswordOtpExpiry(null);
	        user.setResetPasswordOtpAttempts(0);
	        userRepository.save(user);

	        throw new RuntimeException("Invalid or expired OTP");
	    }

	    // Check OTP value
	    if (!passwordEncoder.matches(request.getOtp(), user.getResetPasswordOtp())) {

	        user.setResetPasswordOtpAttempts(user.getResetPasswordOtpAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid OTP");
	    }

	    // OTP is correct → invalidate OTP immediately
	    user.setResetPasswordOtp(null);
	    user.setResetPasswordOtpExpiry(null);
	    user.setResetPasswordOtpAttempts(0);

	    // Generate temporary reset token
	    String resetToken = UUID.randomUUID().toString();

	    user.setResetPasswordToken(resetToken);
	    user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));

	    userRepository.save(user);

	    return resetToken;
	}
	
	
	public void resetPassword(ResetPasswordRequest request) {

	    User user = userRepository.findByResetPasswordToken(request.getResetToken())   
	    		           .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

	    // Check reset token expire?
	    if (user.getResetPasswordTokenExpiry() == null
	            || user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {

	        throw new RuntimeException("Invalid or expired reset token");
	    }

	    // Check password confirmation
	    if (!request.getNewPassword().equals(request.getConfirmPassword())) {

	        throw new RuntimeException("Passwords do not match");
	    }

	    // Hash new password
	    user.setPassword(passwordEncoder.encode(request.getNewPassword()));

	    // Invalidate reset token
	    user.setResetPasswordToken(null);
	    user.setResetPasswordTokenExpiry(null);

	    userRepository.save(user);
	}
	
	
    public UserAuthResponse validateUserCredentials(String email, String rawPassword) {

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                    new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new UserAuthResponse(
              user.getId(),
              user.getName(),
              user.getEmail(),
              null,
              user.getRole().name());
    }
    
    
    public AuthServiceClient.TokenResponse login(String email, String rawPassword) {

        // Step 1: Validate the user email and password
        UserAuthResponse user = validateUserCredentials(email, rawPassword);

        // Step 2: Prepare the token request
        TokenRequest tokenRequest = new TokenRequest(user.getId(), user.getEmail(), user.getRole());

        // Step 3: Ask Authentication Service to generate tokens
        return authServiceClient.generateTokens(tokenRequest);
    }

}
