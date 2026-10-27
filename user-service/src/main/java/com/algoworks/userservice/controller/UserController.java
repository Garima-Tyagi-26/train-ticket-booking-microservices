package com.algoworks.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algoworks.userservice.dto.ChangePasswordRequest;
import com.algoworks.userservice.dto.ForgotPasswordRequest;
import com.algoworks.userservice.dto.RegisterRequest;
import com.algoworks.userservice.dto.ResetPasswordRequest;
import com.algoworks.userservice.dto.UpdateUserRequest;
import com.algoworks.userservice.dto.UserAuthResponse;
import com.algoworks.userservice.dto.UserResponse;
import com.algoworks.userservice.dto.VerifyOtpRequest;
import com.algoworks.userservice.service.UserService;
import com.algoworks.userservice.dto.LoginRequest;
import com.algoworks.userservice.client.AuthServiceClient;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("/users")
public class UserController {

	 private final UserService userService;

	 public UserController(UserService userService) {
	        this.userService = userService;
	 }
	 
	 
	 @Operation(
			summary = "Register a new user",
			description = "Creates a new user account with name, email, password and phone number."
	 )
     @ApiResponses({
		    @ApiResponse(responseCode = "201", description = "User registered successfully"),
		    @ApiResponse(responseCode = "400", description = "Invalid request data"),
		    @ApiResponse(responseCode = "409", description = "Unable to process registration request")
     })
	 
	 @PostMapping
	 public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {

	     UserResponse response = userService.registerUser(request);
         return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }
	 
	 
	 @Operation(
			    summary = "Get user profile by ID",
			    description = "Returns a user profile. A user can access only their own profile, while an admin can access user profiles according to the application's access rules."
	 )
	 @ApiResponses({
			    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
			    @ApiResponse(responseCode = "401", description = "Authentication required"),
			    @ApiResponse(responseCode = "403", description = "Access denied"),
			    @ApiResponse(responseCode = "404", description = "User not found")
	 })
	 
	 @GetMapping("/{id}")
	 public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

	     UserResponse response = userService.getUserById(id);
         return ResponseEntity.ok(response);
	 }
	 
	 
	 @Operation(
			    summary = "Update user profile",
			    description = "Updates the user's name, email, or phone number. Users can update only their own profile, while admin access follows the application's access rules."
	 )
	 @ApiResponses({
			    @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
			    @ApiResponse(responseCode = "400", description = "Invalid request data"),
			    @ApiResponse(responseCode = "401", description = "Authentication required"),
			    @ApiResponse(responseCode = "403", description = "Access denied"),
			    @ApiResponse(responseCode = "404", description = "User not found"),
			    @ApiResponse(responseCode = "409", description = "Unable to process update request")
	 })
	 
	 @PutMapping("/{id}")
	 public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {

	     UserResponse response = userService.updateUser(id, request);
         return ResponseEntity.ok(response);
	 }
	 
	 
	 @Operation(
			    summary = "Get my profile",
			    description = "Returns the profile details of the currently authenticated user."
	 )
	 @ApiResponses({
			    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
			    @ApiResponse(responseCode = "401", description = "Authentication required")
	 })
	 
	 @GetMapping("/me")
	 public UserResponse getMyProfile() {
		 
	     return userService.getMyProfile();
	 }
	 
	 
	 @Operation(
			    summary = "Change my password",
			    description = "Allows the authenticated user to change their password by providing their current password and a new password."
	 )
	 @ApiResponses({
			    @ApiResponse(responseCode = "204", description = "Password changed successfully"),
			    @ApiResponse(responseCode = "400", description = "Invalid request data"),
			    @ApiResponse(responseCode = "401", description = "Authentication required or current password is incorrect")
	 })
	 
	 @PutMapping("/change-password")
	 public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

	     userService.changePassword(request);
	     return ResponseEntity.noContent().build();
	 }
	 
	 
	 @Operation(
			summary = "Request password reset",
			description = "Initiates the password reset process for the user."
	 )
			@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password reset request processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data")
	 })
	 
	 @PostMapping("/forgot-password")
	 public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

	     userService.forgotPassword(request);
         return ResponseEntity.ok("Password reset request processed successfully");
	 }
	 
	 
	 @PostMapping("/verify-otp")
	 public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

	     String resetToken = userService.verifyOtp(request);
         return ResponseEntity.ok(resetToken);
	 }
	 
	 
	 @Operation(
			    summary = "Reset password",
			    description = "Resets the user's password using a valid and non-expired password reset token."
	 )
	 @ApiResponses({
			    @ApiResponse(responseCode = "200", description = "Password reset successfully"),
			    @ApiResponse(responseCode = "400", description = "Invalid request data or reset token")
	 })
	 
	 @PostMapping("/reset-password")
	 public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

	     userService.resetPassword(request);
         return ResponseEntity.ok("Password reset successfully");
	 }
	 
	 
     @PostMapping("/login")
     public ResponseEntity<AuthServiceClient.TokenResponse> login(@Valid @RequestBody LoginRequest request) {

          return ResponseEntity.ok(userService.login(request.getEmail(), request.getPassword()));
     }

}
