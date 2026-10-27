package com.algoworks.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

     private String currentPassword;
	 
     @NotBlank
	 @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
	 @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
	          message = "Password must contain at least one uppercase letter, one lowercase letter, "
	             	      + "one digit, and one special character" )
	 private String newPassword;
	 
	 public ChangePasswordRequest() {}

	 public String getCurrentPassword() {
		 return currentPassword;
	 }

	 public void setCurrentPassword(String currentPassword) {
		 this.currentPassword = currentPassword;
	 }

	 public String getNewPassword() {
		 return newPassword;
	 }

	 public void setNewPassword(String newPassword) {
		 this.newPassword = newPassword;
	 }
}
