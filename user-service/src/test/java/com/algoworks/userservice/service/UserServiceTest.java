package com.algoworks.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.algoworks.userservice.client.AuthServiceClient;
import com.algoworks.userservice.dto.ChangePasswordRequest;
import com.algoworks.userservice.dto.RegisterRequest;
import com.algoworks.userservice.dto.TokenRequest;
import com.algoworks.userservice.dto.UserAuthResponse;
import com.algoworks.userservice.dto.UserResponse;
import com.algoworks.userservice.dto.VerifyOtpRequest;
import com.algoworks.userservice.entity.Role;
import com.algoworks.userservice.entity.User;
import com.algoworks.userservice.exception.EmailAlreadyExistsException;
import com.algoworks.userservice.exception.InvalidCredentialsException;
import com.algoworks.userservice.exception.UserNotFoundException;
import com.algoworks.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // MOCKITO creates the mocks and injects them into UserService.
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    
    @Test
    void registerUser_shouldSaveUserAndReturnUserResponse() {   
    	// Verifying successful registration scenario

          // registration request
          RegisterRequest request = new RegisterRequest();
          request.setName("Somya");
          request.setEmail("somya@example.com");
          request.setPassword("password@12");
          request.setPhone("9876543210");

          // telling MOCKITO what to return 
          when(userRepository.existsByEmail("somya@example.com")).thenReturn(true);

          when(passwordEncoder.encode("password@12")).thenReturn("encodedPassword");

          when(userRepository.save(any(User.class)))
                      .thenAnswer(invocation -> {
                         User user = invocation.getArgument(0);
                         user.setId(1L);
                         return user;           });

         // call the method being tested
         UserResponse response = userService.registerUser(request);

         // check the result
         assertNotNull(response);
         assertEquals(1L, response.getId());
         assertEquals("Somya", response.getName());
         assertEquals("somya@example.com", response.getEmail());
         assertEquals("9876543210", response.getPhone());

         verify(userRepository).existsByEmail("somya@example.com");
         verify(passwordEncoder).encode("password@12");
         verify(userRepository).save(any(User.class));
     }
    
    
    @Test
    void registerUSer_shouldThrowExceptionWhenEmailAlreadyExists() {
    	// Verifying duplicate email scenario
    	
    	RegisterRequest request = new RegisterRequest();
    	request.setName("Somya");
    	request.setEmail("somya@example.com");
    	request.setPassword("password@12");
    	request.setPhone("9876543210");
    	
    	when(userRepository.existsByEmail("somya@example.com")).thenReturn(true);
    	
    	// Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));
        
        verify(userRepository).existsByEmail("somya@example.com");
        // checks that save() method was never called.
        verify(userRepository, never()).save(any(User.class));  
    }

    
    @Test 
    void validateUserCredentials_shouldReturnUserWhenCredentialsAreValid() { 
    	// verifying user detail for login scenario
    	
    	 User user = new User();
    	 user.setId(1L);
    	 user.setName("Garima");
    	 user.setEmail("garima@example.com");
    	 user.setPassword("encodedPassword");
    	 user.setRole(Role.USER);
    	 
    	 when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));
    	 when(passwordEncoder.matches("password@12", "encodedPassword")).thenReturn(true);
    	 
    	 UserAuthResponse response = userService.validateUserCredentials("garima@example.com", "password@12");
    	 
    	 assertNotNull(response);
    	 assertEquals(1L, response.getId());
    	 assertEquals("Garima", response.getName());
    	 assertEquals("garima@example.com", response.getEmail());
    	 assertEquals("USER", response.getRole());
    	 assertNull(response.getPassword());
    	 
    	 verify(userRepository).findByEmail("garima@example.com");
    	 verify(passwordEncoder).matches("password@12", "encodedPassword");
    }
    
    
    @Test
    void validateUserCredentials_shouldThrowExceptionWhenPasswordIsWrong() { 
    	// In login, wrong password scenario
    	
         User user = new User();
         user.setId(1L);
         user.setName("Garima");
         user.setEmail("garima@example.com");
         user.setPassword("encodedPassword");
         user.setRole(Role.USER);

         when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));

         when(passwordEncoder.matches("12345678", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.validateUserCredentials(
                                                              "garima@example.com", "12345678"));

        verify(userRepository).findByEmail("garima@example.com");
        verify(passwordEncoder).matches("12345678", "encodedPassword");
    }
    
    
    @Test
    void validateUserCredentials_shouldThrowExceptionWhenEmailDoesNotExist() {
    	// In login, email doesn't exist scenario

        when(userRepository.findByEmail("punit@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.validateUserCredentials(
                                                              "punit@example.com", "password1#3"));

        verify(userRepository).findByEmail("punit@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
   
    @Test
    void login_ShouldReturnTokensWhenCredentialsAreValid() { 
    	// Verify that login returns access and refresh tokens when credentials are valid.

        // Prepare the user login details.
        String email = "garima@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        // Create a sample user with an ID, email, encoded password, and role.
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(Role.USER);

        // mock repository to return the user when the email is searched.
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // mock password verification to return true because the password is correct.
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // create a mock token response containing the expected access and refresh tokens.
        AuthServiceClient.TokenResponse expectedTokens = new AuthServiceClient.TokenResponse();
        expectedTokens.setAccessToken("mock-access-token");
        expectedTokens.setRefreshToken("mock-refresh-token");

        // mock AuthServiceClient to return the expected tokens for any TokenRequest.
        when(authServiceClient.generateTokens(any(TokenRequest.class))).thenReturn(expectedTokens);

        // Calling login() with valid email and password.
        AuthServiceClient.TokenResponse actualTokens = userService.login(email, rawPassword);

        // Verifying that the login method returns the expected access token.
        assertEquals("mock-access-token", actualTokens.getAccessToken());

        // Verifying that the login method returns the expected refresh token.
        assertEquals("mock-refresh-token", actualTokens.getRefreshToken());

        // Capture the TokenRequest passed to AuthServiceClient so its contents can be checked.
        ArgumentCaptor<TokenRequest> captor = ArgumentCaptor.forClass(TokenRequest.class);
        verify(authServiceClient).generateTokens(captor.capture());

        // Retrieve the actual TokenRequest captured during the method call.
        TokenRequest actualRequest = captor.getValue();

        // Verifying that the correct user ID is included in the token request.
        assertEquals(1L, actualRequest.getUserId());

        // Verifying that the correct email is included in the token request.
        assertEquals(email, actualRequest.getEmail());

        // Verifying that the user role is converted to the expected string.
        assertEquals("USER", actualRequest.getRole());
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verify(authServiceClient, times(1)).generateTokens(any(TokenRequest.class));
     }
    
    
    @Test
    void login_ShouldThrowExceptionWhenPasswordIsWrong() { 
    	// Verifying login should fail when password is incorrect.

        String email = "garima@example.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(Role.USER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThrows( InvalidCredentialsException.class, () -> userService.login(email, rawPassword));

        verify(authServiceClient, never()).generateTokens(any(TokenRequest.class));
    }
    
    
    @Test
    void login_ShouldThrowExceptionWhenEmailDoesNotExist() {  
    	// Verifying login should fail when email doesn't exist.

        //prepare an email that does not exist in the database.
        String email = "unknown@example.com";
        String rawPassword = "password123";

        // Mock the repository to return an empty result for the unknown email.
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Verify that login throws an exception for the unknown email.
        assertThrows( InvalidCredentialsException.class, () -> userService.login(email, rawPassword));

        // Verify that token generation is never attempted for an unknown user.
        verify(authServiceClient, never()).generateTokens(any(TokenRequest.class));
    }
  
    
    @Test
    void getUserById_ShouldReturnUserDetailsWhenUserExists() { 
    	// Verify that service returns correct user details for an existing ID.

        User user = new User();
        user.setId(1L);
        user.setName("Garima");
        user.setEmail("garima@example.com");
        user.setPhone("9876543210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Garima", response.getName());
        assertEquals("garima@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());
    }

    
    @Test
    void getUserById_ShouldThrowExceptionWhenUserDoesNotExist() { 
    	// Verify that requesting nonexistent user produces the expected exception.

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }
    
    
    @Test
    void changePassword_passwordChangedSuccessfully() {
    	// verify the password change of authenticated user

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("password@12");

        User user = new User();
        user.setId(1L);
        user.setEmail("garima@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        // Set up the logged-in user
        var authentication = new UsernamePasswordAuthenticationToken("garima@example.com", null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("oldPassword123", "encodedPassword")).thenReturn(true);

        when(passwordEncoder.encode("password@12")).thenReturn("newEncodedPassword");

        userService.changePassword(request);

        // Verify the new password was encoded
        verify(passwordEncoder).encode("password@12");

        // Verify the user's password was updated
        assertEquals("newEncodedPassword", user.getPassword());

        // Verify the updated user was saved
        verify(userRepository).save(user);
    }
    
    
   @Test
   void changePassword_incorrectCurrentPasswordShouldThrowException() {
	   // verify that current wrong password should Credential Exception
	   
	   ChangePasswordRequest request = new ChangePasswordRequest();
       request.setCurrentPassword("oldPassword123");
       request.setNewPassword("password@12");

       User user = new User();
       user.setId(1L);
       user.setEmail("garima@example.com");
       user.setPassword("encodedPassword");
       user.setRole(Role.USER);
       
       var authentication = new UsernamePasswordAuthenticationToken("garima@example.com", null);

       SecurityContextHolder.getContext().setAuthentication(authentication);
       
       when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));

       when(passwordEncoder.matches("oldPassword123", "encodedPassword")).thenReturn(false);
       
       assertThrows(InvalidCredentialsException.class, ()-> userService.changePassword(request));
       
       verify(passwordEncoder, never()).encode(anyString());
       verify(userRepository, never()).save(any(User.class));
   }
    
    
    @Test
    void changePassword_emailNotExistShouldThrowException() {
    	// verify that mail doesn't exists and throw UserNotFoundException
    	
    	ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("password@12");
    	
    	var authentication = new UsernamePasswordAuthenticationToken("garima@example.com", null);
    	
    	SecurityContextHolder.getContext().setAuthentication(authentication);
    	
    	when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.empty());
    	
    	assertThrows(UserNotFoundException.class, ()-> userService.changePassword(request));
    }
 
    
    @Test
    void verifyOtp_ShouldGenerateResetTokenWhenOtpIsCorrect() {
        // Verify that a valid OTP is consumed and a password-reset token is generated.

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("garima@example.com");
        request.setOtp("123456");

        User user = new User();
        user.setEmail("garima@example.com");
        user.setResetPasswordOtp("encodedOtp");
        user.setResetPasswordOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setResetPasswordOtpAttempts(0);

        when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encodedOtp")).thenReturn(true);

        String resetToken = userService.verifyOtp(request);

        assertNotNull(resetToken);
        assertEquals(resetToken, user.getResetPasswordToken());
        assertNull(user.getResetPasswordOtp());
        assertNull(user.getResetPasswordOtpExpiry());
        assertEquals(0, user.getResetPasswordOtpAttempts());

        verify(userRepository).save(user);
    }

    
    @Test
    void verifyOtp_ShouldIncrementAttemptsWhenOtpIsIncorrect() {
        // Verify that a wrong OTP increases the attempt count to help prevent brute-force attempts.

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("garima@example.com");
        request.setOtp("000000");

        User user = new User();
        user.setEmail("garima@example.com");
        user.setResetPasswordOtp("encodedOtp");
        user.setResetPasswordOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setResetPasswordOtpAttempts(0);

        when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("000000", "encodedOtp")).thenReturn(false);

        assertThrows(RuntimeException.class,() -> userService.verifyOtp(request));

        assertEquals(1, user.getResetPasswordOtpAttempts());
        verify(userRepository).save(user);
    }

    
    @Test
    void verifyOtp_ShouldClearOtpAndThrowExceptionWhenOtpIsExpired() {
        // Verify that an expired OTP is rejected and cleared instead of being accepted.

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("garima@example.com");
        request.setOtp("123456");

        User user = new User();
        user.setEmail("garima@example.com");
        user.setResetPasswordOtp("encodedOtp");
        user.setResetPasswordOtpExpiry(LocalDateTime.now().minusMinutes(1));
        user.setResetPasswordOtpAttempts(1);

        when(userRepository.findByEmail("garima@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,() -> userService.verifyOtp(request));
        assertNull(user.getResetPasswordOtp());
        assertNull(user.getResetPasswordOtpExpiry());
        assertEquals(0, user.getResetPasswordOtpAttempts());

        verify(userRepository).save(user);
    }
}
