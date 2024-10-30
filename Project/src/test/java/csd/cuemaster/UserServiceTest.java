package csd.cuemaster;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.services.EmailService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.user.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository users;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @AfterEach
    void tearDown() {
        users.deleteAll();
    }
  
    @Test
    public void testAddUser_NewUser_Success() {
        // Arrange
        User newUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);

        // Mock behavior
        when(users.findByUsername(newUser.getUsername())).thenReturn(Optional.empty());
        when(encoder.encode(newUser.getPassword())).thenReturn("hashedpassword");
        when(users.save(any(User.class))).thenReturn(newUser);

        // Act
        User savedUser = userService.addUser(newUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals("hashedpassword", savedUser.getPassword()); // Password should be hashed
        assertNotNull(savedUser.getActivationToken());
        assertFalse(savedUser.isEnabled()); // Should still be disabled before activation
    }

    @Test
    public void testAddUser_UserAlreadyExists() {
        // Arrange
        User existingUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);

        // Mock behavior
        when(users.findByUsername(existingUser.getUsername())).thenReturn(Optional.of(existingUser));

        // Act
        User newUser = new User("testuser@gmail.com", "anotherPassword", "ROLE_PLAYER", "normal", false);
        User result = userService.addUser(newUser);

        // Assert
        assertNull(result); // Should return null because the user already exists
        verify(users, never()).save(any(User.class)); // Verify that save() was never called
    }

    @Test
    public void testGoogleLogin_NewUser_Success() {
        // Arrange
        when(users.findByUsername("newuser@gmail.com")).thenReturn(Optional.empty()); // Simulate no existing user
        User newUser = new User("newuser@gmail.com", "nopassword", "ROLE_PLAYER", "google", true);
        when(users.save(any(User.class))).thenReturn(newUser); // Mock user creation

        // Act
        User result = userService.googleLogin("newuser@gmail.com", "ROLE_PLAYER");

        // Assert
        assertEquals("newuser@gmail.com", result.getUsername()); // Should return email of the newly created user
        verify(users).save(any(User.class)); // Ensure the user is saved

        User createdUser = newUser; // Since we are mocking, use the newUser directly
        assertNotNull(createdUser);
        assertEquals("google", createdUser.getProvider());
        assertEquals("ROLE_PLAYER", createdUser.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testAccountActivation_Success() {
        // Arrange
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
        savedUser.setActivationToken("valid-token");

        when(users.findByActivationToken("valid-token")).thenReturn(Optional.of(savedUser)); // Mock finding by token
        when(users.save(any(User.class))).thenReturn(savedUser); // Mock saving the activated user

        // Act
        String result = userService.accountActivation("valid-token");

        // Assert
        assertEquals("Account Activated", result);
        verify(users).save(savedUser); // Ensure the user was saved after activation

        assertTrue(savedUser.isEnabled()); // User should be activated
        assertNull(savedUser.getActivationToken()); // Token should be cleared
    }
    @Test
    void testLoginUser_Success() {
        User user = new User("testuser", "password123", "ROLE_PLAYER", "normal", false);
        User savedUser = new User("testuser", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
    
        when(users.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(encoder.matches(user.getPassword(), savedUser.getPassword())).thenReturn(true);
    
        User result = userService.loginUser(user);
    
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
    

    @Test
    void testLoginUser_IncorrectPassword() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String incorrectPassword = "wrongpassword"; // This will be used for the login attempt
        String encodedPassword = encoder.encode(correctPassword); // Encode the correct password
    
        // Create a user with the correct password
        User savedUser = new User(username, encodedPassword, "ROLE_PLAYER", "normal", false);
    
        // Mock the repository to return the saved user when the username is queried
        when(users.findByUsername(username)).thenReturn(Optional.of(savedUser));
    
        // Create a user object for login attempt with the wrong password
        User loginAttempt = new User(username, incorrectPassword, "ROLE_PLAYER", "normal", false);
    
        // Act
        User result = userService.loginUser(loginAttempt);
    
        // Assert
        assertNull(result); // The result should be null for an incorrect password
    }
    

  

    @Test
    public void testGoogleLogin_ExistingUser() {
        // Arrange
        String email = "existinggoogleuser@gmail.com";
        User existingUser = new User(email, null, "ROLE_PLAYER", "google", true);

        // Mock repository to return an existing Google user
        when(users.findByUsername(email)).thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.googleLogin(email, "ROLE_PLAYER");

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getUsername()); // The result should be the user's email
        verify(users, never()).save(any(User.class)); // No new user should be saved
    }

}
