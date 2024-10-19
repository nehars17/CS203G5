package csd.cuemaster;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
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

    @Test
    public void getUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/users");

        // Create a list of roles as strings
        
        // Encode the password since you're using BCryptPasswordEncoder in your UserService
        // String encodedPassword = encoder.encode("nopassword");
		User user = new User("test@gmail.com", encoder.encode("goodpassword"), "ROLE_ADMIN", "normal", true);
		users.save(user);

        ResponseEntity<User[]> result = restTemplate.getForEntity(uri, User[].class);
        User[] user_array = result.getBody();

        assertEquals(200, result.getStatusCode().value());
        // You can uncomment the following line if you want to assert the number of users
        // assertEquals(1, user_array.length);
        // System.out.println("Response JSON: " + Arrays.toString(user_array));

		
    }
}