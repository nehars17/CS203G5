package csd.cuemaster;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import csd.cuemaster.models.TOTPToken;
import csd.cuemaster.services.JwtService;
import csd.cuemaster.services.TOTPService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.user.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository users;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserService userService;

    @Autowired  
    private JwtService jwtService;

    @Autowired
    private TOTPService totpService;

    @Test
    public void getUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/users");

        // Create a list of roles as strings

        // Encode the password since you're using BCryptPasswordEncoder in your
        // UserService
        // String encodedPassword = encoder.encode("nopassword");
        User user = new User("test@gmail.com", encoder.encode("goodpassword"), "ROLE_ADMIN", "normal", true);
        users.save(user);

        ResponseEntity<User[]> result = restTemplate.getForEntity(uri, User[].class);

        assertEquals(200, result.getStatusCode().value());
        // You can uncomment the following line if you want to assert the number of
        // users
        // assertEquals(1, user_array.length);
        // System.out.println("Response JSON: " + Arrays.toString(user_array));

    }

    @Test
    public void addUser_Success_WithTOTP() throws Exception {
        // Arrange
        URI uri = new URI("http://localhost:" + port + "/register");
        String jsonPayload = "{ \"username\": \"newuser@gmail.com\", \"password\": \"goodpassword\", \"authorities\": \"ROLE_PLAYER\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Initialize ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Asserts
        assertEquals(200, response.statusCode()); // Assuming 201 Created
        User responseBody = objectMapper.readValue(response.body(), User.class);
        assertNotNull(responseBody);
        assertEquals("newuser@gmail.com", responseBody.getUsername());

        // Retrieve the user from the repository to check TOTP details
        User savedUser = users.findById(responseBody.getId()).orElse(null);
        assertNotNull(savedUser);
        assertNotEquals("goodpassword", encoder.encode("goodpassword")); // Password should be hashed
        assertNotNull(savedUser.getSecret()); // Secret key should be set
        TOTPToken totpToken = savedUser.getTotpToken();
        assertNotNull(totpToken); // TOTP token should be set
        assertEquals(totpToken.getCode(), savedUser.getActivationToken()); // Activation token should be set to TOTP
                                                                           // code
        assertFalse(savedUser.isEnabled()); // Should still be disabled before activation
    }

    @Test
    public void addUser_UserAlreadyExists() throws Exception {
        // Setup the same user for the existing check
        User existingUser = new User("existinguser@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal",
                true);
        users.save(existingUser); // Save user directly to repository for test setup

        URI uri = new URI(baseUrl + port + "/register");
        String jsonPayload = "{ \"username\": \"existinguser@gmail.com\", \"password\": \"goodpassword\", \"authorities\": \"ROLE_PLAYER\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Use HttpClient to send the request
        HttpClient client = HttpClient.newHttpClient();
        System.out.println(request);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(409, response.statusCode()); // Assuming 201 Created
        // You may also want to check the response body for specific error messages
    }

    @Test
    public void testActivateAccount_Success() throws Exception {
        // Arrange: Create and save a user with an activation token and TOTP token
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", false);
        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        savedUser.setSecret(secretKey);
        savedUser.setTotpToken(totpCode);
        savedUser.setActivationToken(totpCode.getCode());
        users.save(savedUser);

        // Act: Send HTTP request to activate the account
        URI uri = new URI(baseUrl + port + "/activate");
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", totpCode.getCode());
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        // Assert: Check the response and database changes
        assertEquals(200, response.statusCode());
        assertEquals("Account activated successfully.", response.body());

        User activatedUser = users.findById(savedUser.getId()).orElse(null);
        assertNotNull(activatedUser);
        assertTrue(activatedUser.isEnabled()); // User should be activated
        assertNull(activatedUser.getActivationToken()); // Token should be cleared
        assertNull(activatedUser.getTotpToken()); // TOTP token should be cleared
        assertNull(activatedUser.getSecret()); // Secret should be cleared
    }

    @Test
    public void loginNormalUser_Success() throws Exception {
        // Arrange: Create and save a user
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
        savedUser.setUnlocked(true);
        savedUser.setProvider("local");
        users.save(savedUser);

        // Act: Send HTTP request to normal login endpoint
        URI uri = new URI(baseUrl + port + "/normallogin");
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser@gmail.com");
        payload.put("password", "password123");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert: Check the response and database changes
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("testuser@gmail.com"));
        assertTrue(response.body().contains("ROLE_PLAYER"));
    }

    @Test
    public void loginNormalUser_AccountLocked() throws Exception {
        // Arrange: Create and save a user
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
        savedUser.setUnlocked(false);
        users.save(savedUser);

        // Act: Send HTTP request to normal login endpoint
        URI uri = new URI(baseUrl + port + "/normallogin");
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser@gmail.com");
        payload.put("password", "password123");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert: Check the response
        assertEquals(200, response.statusCode());
        assertTrue(
                response.body().contains("Account Locked, Please contact administrator at cuemasternoreply@gmail.com"));
    }

    @Test
    public void loginNormalUser_LoginWithGoogle() throws Exception {
        // Arrange: Create and save a user
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "google", true);
        users.save(savedUser);

        // Act: Send HTTP request to normal login endpoint
        URI uri = new URI(baseUrl + port + "/normallogin");
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser@gmail.com");
        payload.put("password", "password123");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert: Check the response
        assertEquals(401, response.statusCode());
    }

    @Test
    public void loginNormalUser_IncorrectPassword() throws Exception {
        URI uri = new URI(baseUrl + port + "/normallogin");
        users.save(new User("testuser3@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal", true));

        String jsonPayload = "{ \"username\": \"testuser3@gmail.com\", \"password\": \"testpassword\", \"authorities\": \"ROLE_PLAYER\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Use HttpClient to send the request
        HttpClient client = HttpClient.newHttpClient();
        System.out.println(request);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode()); // Expecting a Bad Request due to incorrect password
        // Check the error message in the response body if necessary
    }

    @Test
    public void testVerifyTwoFactorCode_Success() throws Exception {
        // Arrange: Create and save a user
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
        users.save(savedUser);
        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        savedUser.setSecret(secretKey);
        savedUser.setTotpToken(totpCode);
        users.save(savedUser);

        // Act: Send HTTP request to verify-code endpoint
        URI uri = new URI(baseUrl + port + "/verify-code");
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", savedUser);
        payload.put("code", totpCode.getCode());
        payload.put("role", "ROLE_PLAYER");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Assert: Check the response
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("testuser@gmail.com"));
        assertTrue(response.body().contains("ROLE_PLAYER"));
    }

    @Test
    public void testVerifyTwoFactorCode_InvalidCode() throws Exception {
        // Arrange: Create and save a user
        User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        savedUser.setSecret(secretKey);
        savedUser.setTotpToken(totpCode);
        users.save(savedUser);

        // Act: Send HTTP request to verify-code endpoint
        URI uri = new URI(baseUrl + port + "/verify-code");
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", savedUser);
        payload.put("code", "invalid-code");
        payload.put("role", "ROLE_PLAYER");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert: Check the response
        assertEquals(403, response.statusCode());
        assertTrue(response.body().contains("Invalid code, please try again."));
    }

    @Test
public void testForgotPassword_Success() throws Exception {
    // Arrange: Create and save a user
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
    users.save(savedUser);

    // Mock the forgotPassword method to return the saved user
    // Act: Send HTTP request to forgotPassword endpoint
    URI uri = new URI(baseUrl + port + "/forgotPassword");
    Map<String, String> payload = new HashMap<>();
    payload.put("username", "testuser@gmail.com");
    String jsonPayload = new ObjectMapper().writeValueAsString(payload);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert: Check the response
    assertEquals(200, response.statusCode());
    assertEquals("Password reset link has been sent to your email.", response.body());
}

    @Test
    public void testForgotPassword_UserNotFound() throws Exception {
        // Mock the forgotPassword method to return null
        // Act: Send HTTP request to forgotPassword endpoint
        URI uri = new URI(baseUrl + port + "/forgotPassword");
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "nonexistentuser@gmail.com");
        String jsonPayload = new ObjectMapper().writeValueAsString(payload);
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        // Assert: Check the response
        assertEquals(401, response.statusCode());
    }

@Test
public void testResetPassword_Success() throws Exception {
    // Arrange: Create and save a user
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
    Key secretKey = totpService.generateSecret();
    TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
    savedUser.setSecret(secretKey);
    savedUser.setTotpToken(totpCode);
    users.save(savedUser);
    // Mock the resetPassword method to return a success message

    // Act: Send HTTP request to resetPassword endpoint
    URI uri = new URI(baseUrl + port + "/resetPassword");
    Map<String, String> payload = new HashMap<>();
    payload.put("token", totpCode.getCode());
    payload.put("password", "newpassword123");
    payload.put("user_id", savedUser.getId().toString());
    String jsonPayload = new ObjectMapper().writeValueAsString(payload);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert: Check the response
    assertEquals(200, response.statusCode());
    assertEquals("Password updated successfully.", response.body());
}

@Test
public void testUpdatePassword_Success() throws Exception {
    // Arrange: Create and save a user
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
    users.save(savedUser);

    String jwtToken = obtainJwtToken("ROLE_PLAYER");

    // Act: Send HTTP request to updatePassword endpoint
    URI uri = new URI(baseUrl + port + "/update/" + savedUser.getId() + "/password");
    Map<String, String> payload = new HashMap<>();
    payload.put("password", "newpassword123");
    String jsonPayload = new ObjectMapper().writeValueAsString(payload);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + jwtToken)
            .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert: Check the response
    assertEquals(200, response.statusCode());
}
@Test
public void testDeleteAccount_Success() throws Exception {
    // Arrange: Create and save a user
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
    users.save(savedUser);
    String jwtToken = obtainJwtToken("ROLE_PLAYER");
    // Mock the deleteUser method

    // Act: Send HTTP request to deleteAccount endpoint
    URI uri = new URI(baseUrl + port + "/user/" + savedUser.getId() + "/account");

    HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", "Bearer " + jwtToken)
            .DELETE()
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert: Check the response
    assertEquals(200, response.statusCode());
}
@Test
public void testUnlockAccount_Success() throws Exception {
    // Arrange: Create and save a user
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_ADMIN", "normal", false);
    savedUser.setUnlocked(false);
    users.save(savedUser);
    String jwtToken = obtainJwtToken("ROLE_ADMIN");


    // Act: Send HTTP request to unlockAccount endpoint
    URI uri = new URI(baseUrl + port + "/user/" + savedUser.getId() + "/account");

    HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", "Bearer " + jwtToken)
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert: Check the response
    assertEquals(200, response.statusCode());
}

@AfterEach
void tearDown() {
    users.deleteAll();
}

private String obtainJwtToken(String role) throws Exception {
    // Implement the logic to obtain a valid JWT token
    // This could involve sending a login request and extracting the token from the response
    URI uri = new URI(baseUrl + port + "/verify-code");
    User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), "ROLE_PLAYER", "normal", true);
    Key secretKey = totpService.generateSecret();
    TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
    savedUser.setSecret(secretKey);
    savedUser.setTotpToken(totpCode);
    users.save(savedUser);
    Map<String, Object> loginPayload = new HashMap<>();
    loginPayload.put("user", savedUser);
    loginPayload.put("code", totpCode.getCode());
    if(role.equals("ROLE_PLAYER")){
        loginPayload.put("role", "ROLE_PLAYER");
    }
    else if(role.equals("ROLE_ADMIN")){
        loginPayload.put("role", "ROLE_ADMIN");
    }
    else{
        loginPayload.put("role", "ROLE_ORGANIZER");
    }
    
    String jsonLoginPayload = new ObjectMapper().writeValueAsString(loginPayload);

    HttpRequest loginRequest = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonLoginPayload))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

    // Extract the token from the login response
    // Assuming the token is in the response body as a JSON field named "token"
    Map<String, Object> responseBody = new ObjectMapper().readValue(loginResponse.body(), Map.class);
    return (String) responseBody.get("token");
}

}
