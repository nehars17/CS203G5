package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;

import csd.cuemaster.profile.*;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProfileIntegrationTest {

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
    private BCryptPasswordEncoder encoder;

    @AfterEach
    void tearDown() {
        profiles.deleteAll();
        users.deleteAll();
    }

    // BRYAN TESTS
    @Test
    public void testGetAllProfilesWithoutRole() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
        Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York", "images/player1.jpg",
                playerUser);
        profiles.save(playerProfile);

        User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal",
                true);
        users.save(organizerUser);
        Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                "images/organizer1.jpg", "Organization", organizerUser);
        profiles.save(organizerProfile);

        URI uri = new URI(baseUrl + port + "/profiles");

        // Act
        ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
        assertEquals(2, profiles.length);
    }

    @Test
    public void testGetAllPlayerProfiles() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
        Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York", "images/player1.jpg",
                playerUser);
        profiles.save(playerProfile);

        User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal",
                true);
        users.save(organizerUser);
        Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                "images/organizer1.jpg", "Organization", organizerUser);
        profiles.save(organizerProfile);

        URI uri = new URI(baseUrl + port + "/profiles?role=Player");

        // Act
        ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
        assertEquals(1, profiles.length);
    }

    @Test
    public void testGetAllIOrganiserProfiles() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
        Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York", "images/player1.jpg",
                playerUser);
        profiles.save(playerProfile);

        User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal",
                true);
        users.save(organizerUser);
        Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                "images/organizer1.jpg", "Organization", organizerUser);
        profiles.save(organizerProfile);

        User organizerUser2 = new User("fang@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal",
                true);
        users.save(organizerUser2);
        Profile organizerProfile2 = new Profile("Sheryl", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                "images/organizer1.jpg", "Organization", organizerUser2);
        profiles.save(organizerProfile2);

        URI uri = new URI(baseUrl + port + "/profiles?role=Organizer");

        // Act
        ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
        assertEquals(2, profiles.length);
    }

    @Test
    public void testEmptyProfiles() throws Exception {
        // Arrange: Create test users and profiles
        URI uri = new URI(baseUrl + port + "/profiles");

        // Act
        ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
        assertEquals(0, profiles.length);
    }

    @Test
    public void testGetUserProfile() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
        Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York", "images/player1.jpg",
                playerUser);
        profiles.save(playerProfile);

        User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal",
                true);
        users.save(organizerUser);
        Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                "images/organizer1.jpg", "Organization", organizerUser);
        profiles.save(organizerProfile);

        // Save another user for additional test case
        User anotherUser = new User("alice@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(anotherUser);
        Profile anotherProfile = new Profile("Alice", "Johnson", LocalDate.of(1992, 8, 10), "Chicago",
                "images/player2.jpg", anotherUser);
        profiles.save(anotherProfile);

        // Define the URI for the specific user profile to retrieve (use the user ID)
        URI uri = new URI(baseUrl + port + "/profile/" + playerUser.getId());

        // Act: Send the GET request for the specific user profile
        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);
        Profile profile = result.getBody();

        // Assert: Verify the response status code and the returned profile
        assertEquals(200, result.getStatusCode().value(), "Expected HTTP status code 200");
        assertNotNull(profile, "Profile should not be null");
        assertEquals(playerProfile.getFirstname(), profile.getFirstname(), "First name should match");
        assertEquals(playerProfile.getLastname(), profile.getLastname(), "Last name should match");
        assertEquals(playerProfile.getBirthdate(), profile.getBirthdate(), "Birthdate should match");
        assertEquals(playerProfile.getBirthlocation(), profile.getBirthlocation(), "Birth location should match");
        assertEquals(playerProfile.getProfilephotopath(), profile.getProfilephotopath(), "Profile photo should match");
    }

    @Test
    public void testGetProfileThrowsUserNotFoundException() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
        Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York", "images/player1.jpg", playerUser);
        profiles.save(playerProfile);
    
        // We will attempt to get a profile for a non-existent user (with a non-existing userId)
        Long nonExistentUserId = 999L;
    
        // Act & Assert: Verify that the exception is thrown when a profile is requested for a non-existent user
        URI uri = new URI(baseUrl + port + "/profile/" + nonExistentUserId);
    
        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void testGetProfileThrowsUserProfileNotFoundException() throws Exception {
        // Arrange: Create test users and profiles
        User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        users.save(playerUser);
    
        // Act & Assert: Verify that the exception is thrown when a profile is requested for a non-existent user
        URI uri = new URI(baseUrl + port + "/profile/" + playerUser.getId());
    
        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

        assertEquals(404, result.getStatusCode().value());
    }
}
