// package csd.cuemaster;

// import java.security.Key;
// import java.time.Instant;
// import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// import csd.cuemaster.models.TOTPToken;
// import csd.cuemaster.services.EmailService;
// import csd.cuemaster.services.TOTPService;
// import csd.cuemaster.user.User;
// import csd.cuemaster.user.UserNotFoundException;
// import csd.cuemaster.user.UserRepository;
// import csd.cuemaster.user.UserServiceImpl;

// @ExtendWith(MockitoExtension.class)
// class UserServiceTest {

//     @Mock
//     private UserRepository users;

//     @InjectMocks
//     private UserServiceImpl userService;

//     @Mock
//     private EmailService emailService;

    
//     @Mock
//     private TOTPService totpService;

//     @Mock
//     private BCryptPasswordEncoder encoder;

//     @AfterEach
//     void tearDown() {
//         users.deleteAll();
//     }
  
//     @Test
// public void testAddUser_NewUser_Success_WithTOTP() throws Exception {
//     // Arrange
//     User newUser = new User("testuser@gmail.com", "password123", "ROLE_PLAYER", "normal", false);
//     Key secretKey = mock(Key.class);
//     TOTPToken totpToken = new TOTPToken("123456", Instant.now().plusSeconds(300)); // Example TOTP code

//     // Mock behavior
//     when(users.findByUsername(newUser.getUsername())).thenReturn(Optional.empty());
//     when(encoder.encode(newUser.getPassword())).thenReturn("hashedpassword");
//     when(totpService.generateSecret()).thenReturn(secretKey);
//     when(totpService.generateTOTPToken(secretKey)).thenReturn(totpToken);
//     when(users.save(any(User.class))).thenReturn(newUser);

//     // Act
//     User savedUser = userService.addUser(newUser);

//     // Assert
//     assertNotNull(savedUser);
//     assertEquals("hashedpassword", savedUser.getPassword()); // Password should be hashed
//     assertEquals(secretKey, savedUser.getSecret()); // Secret key should be set
//     assertEquals(totpToken, savedUser.getTotpToken()); // TOTP token should be set
//     assertEquals(totpToken.getCode(), savedUser.getActivationToken()); // Activation token should be set to TOTP code
//     assertFalse(savedUser.isEnabled()); // Should still be disabled before activation
// }

//     @Test
//     public void testAddUser_UserAlreadyExists() throws Exception {
//         // Arrange
//         User existingUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);

//         // Mock behavior
//         when(users.findByUsername(existingUser.getUsername())).thenReturn(Optional.of(existingUser));

//         // Act
//         User newUser = new User("testuser@gmail.com", "anotherPassword", "ROLE_PLAYER", "normal", false);
//         User result = userService.addUser(newUser);

//         // Assert
//         assertNull(result); // Should return null because the user already exists
//         verify(users, never()).save(any(User.class)); // Verify that save() was never called
//     }

//     @Test
//     public void testGoogleLogin_NewUser_Success() {
//         // Arrange
//         when(users.findByUsername("newuser@gmail.com")).thenReturn(Optional.empty()); // Simulate no existing user
//         User newUser = new User("newuser@gmail.com", "nopassword", "ROLE_PLAYER", "google", true);
//         when(users.save(any(User.class))).thenReturn(newUser); // Mock user creation

//         // Act
//         User result = userService.googleLogin("newuser@gmail.com", "ROLE_PLAYER");

//         // Assert
//         assertEquals("newuser@gmail.com", result.getUsername()); // Should return email of the newly created user
//         verify(users).save(any(User.class)); // Ensure the user is saved

//         User createdUser = newUser; // Since we are mocking, use the newUser directly
//         assertNotNull(createdUser);
//         assertEquals("google", createdUser.getProvider());
//         assertEquals("ROLE_PLAYER", createdUser.getAuthorities().iterator().next().getAuthority());
//     }

//     @Test
//     public void testAccountActivation_Success() throws Exception {
//         // Arrange
//         User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
//         TOTPToken totpToken = new TOTPToken("123456", Instant.now().plusSeconds(5000)); // Example TOTP code
//         savedUser.setActivationToken("123456");
//         savedUser.setTotpToken(totpToken);
//         Key secretKey = mock(Key.class);
//         savedUser.setSecret(secretKey);
//         when(users.findByActivationToken("123456")).thenReturn(Optional.of(savedUser)); // Mock finding by token
//         when(totpService.validateTOTPToken(secretKey, totpToken)).thenReturn(true); // Mock TOTP validation
//         when(users.save(any(User.class))).thenReturn(savedUser); // Mock saving the activated user
//         // Act
//         String result = userService.accountActivation("123456");

//         // Assert
//         assertEquals("Account activated successfully.", result);
//         verify(users).save(savedUser); // Ensure the user was saved after activation

//         assertTrue(savedUser.isEnabled()); // User should be activated
//         assertNull(savedUser.getActivationToken()); // Token should be cleared
//     }
 

//     @Test
//     public void testEmailAuth_Success() throws Exception {
//         // Arrange
//         String code = "123456";
//         String username = "testuser@gmail.com";
//         User foundUser = new User(username, encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
//         TOTPToken totpToken = new TOTPToken(code, Instant.now().plusSeconds(300)); // Example TOTP code
//         foundUser.setTotpToken(totpToken);
//         Key secretKey = mock(Key.class);
//         foundUser.setSecret(secretKey);

//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.of(foundUser));
//         when(totpService.validateTOTPToken(secretKey, totpToken)).thenReturn(true);
//         when(users.save(any(User.class))).thenReturn(foundUser);

//         // Act
//         User result = userService.EmailAuth(code, username);

//         // Assert
//         assertNotNull(result);
//         assertEquals(username, result.getUsername());
//         assertNull(result.getTotpToken()); // TOTP token should be cleared
//         assertNull(result.getSecret()); // Secret should be cleared
//         assertNull(result.getActivationToken()); // Activation token should be cleared
//     }

//     @Test
//     public void testEmailAuth_InvalidUsername() throws Exception {
//         // Arrange
//         String code = "123456";
//         String username = "invaliduser@gmail.com";
        
//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
//             userService.EmailAuth(code, username);
//         });

//         assertEquals("User not found", exception.getMessage());
//     }


//     @Test
//     public void testEmailAuth_InvalidCode() throws Exception {
//         // Arrange
//         String code = "123456";
//         String wrongCode = "654321";
//         String username = "testuser@gmail.com";
//         User foundUser = new User(username, encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
//         TOTPToken totpToken = new TOTPToken(wrongCode, Instant.now().plusSeconds(300)); // Example wrong TOTP code
//         foundUser.setTotpToken(totpToken);
//         Key secretKey = mock(Key.class);
//         foundUser.setSecret(secretKey);

//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.of(foundUser));

//         // Act
//         User result = userService.EmailAuth(code, username);

//         // Assert
//         assertNull(result); // Should return null for invalid code
//     }

//     @Test
//     public void testEmailAuth_ExpiredToken() throws Exception {
//         // Arrange
//         String code = "123456";
//         String username = "testuser@gmail.com";
//         User foundUser = new User(username, encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
//         TOTPToken expiredToken = new TOTPToken(code, Instant.now().minusSeconds(300)); // Expired TOTP code
//         foundUser.setTotpToken(expiredToken);
//         Key secretKey = mock(Key.class);
//         foundUser.setSecret(secretKey);

//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.of(foundUser));
//         when(totpService.validateTOTPToken(secretKey, expiredToken)).thenReturn(false);

//         // Act
//         User result = userService.EmailAuth(code, username);

//         // Assert
//         assertNull(result); // Should return null for expired token
//     }



//     @Test
//     void testLoginUser_Success() throws Exception {
//         User user = new User("testuser", "password123", "ROLE_PLAYER", "normal", false);
//         User savedUser = new User("testuser", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
    
//         when(users.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
//         when(encoder.matches(user.getPassword(), savedUser.getPassword())).thenReturn(true);
    
//         User result = userService.loginUser(user);
    
//         assertNotNull(result);
//         assertEquals("testuser", result.getUsername());
//     }


    

//     @Test
//     void testLoginUser_IncorrectPassword() throws Exception {
//         // Arrange
//         String username = "testuser";
//         String correctPassword = "password123";
//         String incorrectPassword = "wrongpassword"; // This will be used for the login attempt
//         String encodedPassword = encoder.encode(correctPassword); // Encode the correct password
    
//         // Create a user with the correct password
//         User savedUser = new User(username, encodedPassword, "ROLE_PLAYER", "normal", false);
    
//         // Mock the repository to return the saved user when the username is queried
//         when(users.findByUsername(username)).thenReturn(Optional.of(savedUser));
    
//         // Create a user object for login attempt with the wrong password
//         User loginAttempt = new User(username, incorrectPassword, "ROLE_PLAYER", "normal", false);
    
//         // Act
//         User result = userService.loginUser(loginAttempt);
    
//         // Assert
//         assertNull(result); // The result should be null for an incorrect password
//     }
    

  

//     @Test
//     public void testGoogleLogin_ExistingUser() {
//         // Arrange
//         String email = "existinggoogleuser@gmail.com";
//         User existingUser = new User(email, null, "ROLE_PLAYER", "google", true);

//         // Mock repository to return an existing Google user
//         when(users.findByUsername(email)).thenReturn(Optional.of(existingUser));

//         // Act
//         User result = userService.googleLogin(email, "ROLE_PLAYER");

//         // Assert
//         assertNotNull(result);
//         assertEquals(email, result.getUsername()); // The result should be the user's email
//         verify(users, never()).save(any(User.class)); // No new user should be saved
//     }


//     @Test
//     public void testForgotPassword_Success() throws Exception {
//         // Arrange
//         String username = "testuser@gmail.com";
//         User foundUser = new User(username, encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
//         Key secretKey = mock(Key.class);
//         TOTPToken totpToken = new TOTPToken("123456", Instant.now().plusSeconds(300)); // Example TOTP code

//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.of(foundUser));
//         when(totpService.generateSecret()).thenReturn(secretKey);
//         when(totpService.generateTOTPToken(secretKey)).thenReturn(totpToken);
//         when(users.save(any(User.class))).thenReturn(foundUser);

//         // Act
//         User result = userService.forgotPassword(username);

//         // Assert
//         assertNotNull(result);
//         assertEquals(username, result.getUsername());
//         assertEquals(secretKey, result.getSecret()); // Secret key should be set
//         assertEquals(totpToken, result.getTotpToken()); // TOTP token should be set
//     }

//     @Test
//     public void testForgotPassword_UserNotFound() throws Exception {
//         // Arrange
//         String username = "invaliduser@gmail.com";

//         // Mock behavior
//         when(users.findByUsername(username)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
//             userService.forgotPassword(username);
//         });

//         assertEquals("User not found", exception.getMessage());
//     }

//      @Test
//     public void testUnlockAccount_Success() throws Exception {
//         // Arrange
//         Long userId = 1L;
//         User foundUser = new User("testuser@gmail.com", "password123", "ROLE_PLAYER", "normal", false);
//         foundUser.setUnlocked(false);

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(foundUser));
//         doNothing().when(emailService).sendUnlockedEmail(anyString());
//         when(users.save(any(User.class))).thenReturn(foundUser);

//         // Act
//         boolean result = userService.unlockAccount(userId);

//         // Assert
//         assertTrue(result);
//         verify(users).save(foundUser); // Ensure the user was saved after unlocking
//         assertTrue(foundUser.isUnlocked()); // User should be unlocked
//         verify(emailService).sendUnlockedEmail(foundUser.getUsername()); // Ensure email was sent
//     }

//     @Test
//     public void testUnlockAccount_UserNotFound() {
//         // Arrange
//         Long userId = 1L;
//         when(users.findById(userId)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
//             userService.unlockAccount(userId);
//         });

//         assertEquals("User not found", exception.getMessage());
//     }


//     //cannot Run
//      @Test
//     public void testUpdatePassword_Success() {
//         // Arrange
//         Long userId = 1L;
//         User updatedUser = new User();
//         updatedUser.setPassword("newpassword123");
//         User foundUser = new User();
//         foundUser.setPassword("oldpassword");

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(foundUser));
//         when(encoder.encode("newpassword123")).thenReturn("hashednewpassword");
//         when(users.save(any(User.class))).thenReturn(foundUser);

//         // Act
//         userService.updatePassword(userId, updatedUser);

//         // Assert
//         // verify(users).save(foundUser);
//         assertEquals("hashednewpassword", foundUser.getPassword());
//     }

//     @Test
//     public void testUpdatePassword_UserNotFound() {
//         // Arrange
//         Long userId = 1L;
//         User updatedUser = new User();
//         updatedUser.setPassword("newpassword123");

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UserNotFoundException.class, () -> {
//             userService.updatePassword(userId, updatedUser);
//         });

//         assertEquals("User with UserID: 1 not found.", exception.getMessage());
//     }

    
//     @Test
//     public void testResetPassword_Success() throws Exception {
//         // Arrange
//         Long userId = 1L;
//         String newPassword = "newpassword123";
//         String token = "123456";
//         User foundUser = new User();
//         foundUser.setTotpToken(new TOTPToken(token, Instant.now().plusSeconds(300)));
//         foundUser.setSecret(mock(Key.class));

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(foundUser));
//         when(totpService.validateTOTPToken(any(), any())).thenReturn(true);
//         when(encoder.encode(newPassword)).thenReturn("hashednewpassword");
//         when(users.save(any(User.class))).thenReturn(foundUser);

//         // Act
//         String result = userService.resetPassword(userId, newPassword, token);

//         // Assert
//         assertEquals("Password updated successfully.", result);
//         verify(users).save(foundUser);
//         assertEquals("hashednewpassword", foundUser.getPassword());
//         assertNull(foundUser.getTotpToken());
//         assertNull(foundUser.getSecret());
//     }

//     @Test
//     public void testResetPassword_UserNotFound() {
//         // Arrange
//         Long userId = 1L;
//         String newPassword = "newpassword123";
//         String token = "123456";

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UserNotFoundException.class, () -> {
//             userService.resetPassword(userId, newPassword, token);
//         });

//         assertEquals("User with UserID: 1 not found.", exception.getMessage());
//     }

//     @Test
//     public void testResetPassword_InvalidToken() throws Exception {
//         // Arrange
//         Long userId = 1L;
//         String newPassword = "newpassword123";
//         String token = "123456";
//         User foundUser = new User();
//         foundUser.setTotpToken(new TOTPToken("wrongtoken", Instant.now().plusSeconds(300)));
//         foundUser.setSecret(mock(Key.class));

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(foundUser));

//         // Act
//         String result = userService.resetPassword(userId, newPassword, token);

//         // Assert
//         assertEquals("Token mismatch or token not found.", result);
//     }

//     @Test
//     public void testResetPassword_ExpiredToken() throws Exception {
//         // Arrange
//         Long userId = 1L;
//         String newPassword = "newpassword123";
//         String token = "123456";
//         User foundUser = new User();
//         foundUser.setTotpToken(new TOTPToken(token, Instant.now().minusSeconds(300))); // Token expired
//         foundUser.setSecret(mock(Key.class));

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(foundUser));
//         when(totpService.validateTOTPToken(any(), any())).thenReturn(false);

//         // Act
//         String result = userService.resetPassword(userId, newPassword, token);

//         // Assert
//         assertEquals("Invalid or expired token.", result);
//     }

//     @Test
//     public void testDeleteUser_Success() {
//         // Arrange
//         Long userId = 1L;
//         User user = new User();

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.of(user));
//         doNothing().when(users).delete(user);

//         // Act
//         userService.deleteUser(userId);

//         // Assert
//         verify(users).delete(user);
//     }

//     @Test
//     public void testDeleteUser_UserNotFound() {
//         // Arrange
//         Long userId = 1L;

//         // Mock behavior
//         when(users.findById(userId)).thenReturn(Optional.empty());

//         // Act & Assert
//         Exception exception = assertThrows(UserNotFoundException.class, () -> {
//             userService.deleteUser(userId);
//         });

//         assertEquals("User with UserID: 1 not found.", exception.getMessage());
//     }






// }
