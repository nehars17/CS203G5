package csd.cuemaster;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.user.EmailService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.user.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CueMasterApplicationTests {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ProfileRepository profiles;

	@Autowired
	private UserRepository users;

	@Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
	
    @Autowired
	private BCryptPasswordEncoder encoder;


	@AfterEach
	void tearDown() {
		profiles.deleteAll();
		users.deleteAll();
	}
	// @Test
	// public void getUser_Success() throws Exception {
	// URI uri = new URI(baseUrl + port + "/users");
	// users.save(new User("bryan@gmail.com", "goodpassword",
	// Arrays.asList("ROLE_ADMIN"), "Normal", true));
	// var result = restTemplate.getForEntity(uri, User[].class);
	// User[] user_array = result.getBody();

	// assertEquals(200, result.getStatusCode().value());
	// assertEquals(1, user_array.length);
	// }
	@Test
	public void testAddUser_Success() {
		User newUser = new User("testuser@gmail.com", "password123", Arrays.asList("ROLE_PLAYER"), "normal", false);
		User savedUser = userService.addUser(newUser);

		assertNotNull(savedUser);
		assertNotEquals("password123", savedUser.getPassword()); // Password should be hashed
		assertNotNull(savedUser.getActivationToken());
		assertFalse(savedUser.isEnabled()); // Should still be disabled before activation
	}

	@Test
	public void testAddUser_UserAlreadyExists() {
		// Create a user and save it to the database
		User existingUser = new User("testuser@gmail.com", "password123", Arrays.asList("ROLE_PLAYER"), "normal", true);
		userService.addUser(existingUser);

		// Try to register the same user again
		User newUser = new User("testuser@gmail.com", "anotherPassword", Arrays.asList("ROLE_PLAYER"), "normal", false);
		User result = userService.addUser(newUser);

		assertNull(result); // Should return null because user already exists
	}

	// @Test
	// public void testGoogleLogin_NewUser() {
	// 	String result = userService.googleLogin("newuser@gmail.com", "ROLE_GOOGLE_USER");

	// 	assertEquals("newuser@gmail.com", result); // Should return email of the newly created user
	// 	User createdUser = users.findByUsername("newuser@gmail.com").orElse(null);
	// 	assertNotNull(createdUser);
	// 	assertEquals("google", createdUser.getProvider());
	// 	assertEquals(Arrays.asList("ROLE_GOOGLE_USER"), createdUser.getAuthorities());
	// }

	// @Test
	// public void testAccountActivation_Success() {
	// 	User savedUser = new User("testuser@gmail.com", encoder.encode("password123"), Arrays.asList("ROLE_PLAYER"),
	// 			"normal", false);
	// 	savedUser.setActivationToken("valid-token");
	// 	users.save(savedUser);

	// 	String result = userService.accountActivation("valid-token");

	// 	assertEquals("Account Activated", result);
	// 	User activatedUser = users.findByUsername("testuser@gmail.com").orElse(null);
	// 	assertNotNull(activatedUser);
	// 	assertTrue(activatedUser.isEnabled()); // User should be activated
	// 	assertNull(activatedUser.getActivationToken()); // Token should be cleared
	// }

}
