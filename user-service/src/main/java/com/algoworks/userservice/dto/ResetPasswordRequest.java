package com.algoworks.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

	    @NotBlank
	    private String resetToken;

	    @NotBlank
	    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
	    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
	             message = "Password must contain at least one uppercase letter, one lowercase letter, "
	             	    	+ "one digit, and one special character" )
	    private String newPassword;

	    @NotBlank
	    private String confirmPassword;

	    public ResetPasswordRequest() {}

	    public String getResetToken() {
	        return resetToken;
	    }

	    public void setResetToken(String resetToken) {
	        this.resetToken = resetToken;
	    }

	    public String getNewPassword() {
	        return newPassword;
	    }

	    public void setNewPassword(String newPassword) {
	        this.newPassword = newPassword;
	    }

	    public String getConfirmPassword() {
	        return confirmPassword;
	    }

	    public void setConfirmPassword(String confirmPassword) {
	        this.confirmPassword = confirmPassword;
	    }
	  
}
