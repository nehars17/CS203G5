package csd.cuemaster;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import csd.cuemaster.services.TOTPService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

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
    public void addUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/register");
        String jsonPayload = "{ \"username\": \"newuser@gmail.com\", \"password\": \"goodpassword\", \"authorities\": \"ROLE_PLAYER\"}";
        // Clear previous users to avoid conflicts

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Use HttpClient to send the request
        HttpClient client = HttpClient.newHttpClient();
        System.out.println(request);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode()); // Assuming 201 Created
        User responseBody = new ObjectMapper().readValue(response.body(), User.class);
        assertNotNull(responseBody);
        assertEquals("newuser@gmail.com", responseBody.getUsername());
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

    @SuppressWarnings("deprecation")
    @Test
    public void accountActivation_Success() throws Exception {
        // First, save a user with a valid activation token
        User user = new User("activatableuser@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal",
                false);
        user.setActivationToken("valid-token");
        users.save(user);

        URI uri = new URI(baseUrl + port + "/activate?token=valid-token");

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Account Activated", response.getBody());
        // Verify that the user is now enabled
        User activatedUser = users.findByUsername("activatableuser@gmail.com").orElse(null);
        assertNotNull(activatedUser);
        assertTrue(activatedUser.isEnabled());
        assertNull(activatedUser.getActivationToken()); // Token should be cleared
    }

    @Test
    public void loginUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/normallogin");
    
        // Simulate the user being saved
        users.save(new User("testuser2@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal", true));
    
        String jsonPayload = "{ \"username\": \"testuser2@gmail.com\", \"password\": \"goodpassword\", \"authorities\": \"ROLE_PLAYER\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    
        // Use HttpClient to send the request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        // Check the response status
        assertEquals(200, response.statusCode());
    
        // Parse the response body into a Map
        Map<String, Object> responseBody = new ObjectMapper().readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    
        // Ensure the response body is not null
        assertNotNull(responseBody);
    
        // Deserialize the user from the map to a User object
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        User user = mapper.convertValue(userMap, User.class);
    
        // Verify the username from the User object
        assertEquals("testuser2@gmail.com", user.getUsername());
    
        // Verify the JWT token
        String token = (String) responseBody.get("token");
        assertNotNull(token);  // You can also validate the structure of the token here
    
        // Verify the role
        String role = (String) responseBody.get("role");
        assertEquals("ROLE_PLAYER", role);
    }
    

    @Test
    public void loginUser_IncorrectPassword() throws Exception {
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

}
