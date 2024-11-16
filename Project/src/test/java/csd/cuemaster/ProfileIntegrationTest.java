package csd.cuemaster;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.profile.*;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
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
        private TournamentRepository tournaments;

        @Autowired
        private MatchRepository matches;

        @Autowired
        private BCryptPasswordEncoder encoder;

        @AfterEach
        void tearDown() {
                profiles.deleteAll();
                matches.deleteAll();
                tournaments.deleteAll();
                users.deleteAll();
        }

        // BRYAN TESTS
        @Test
        public void testGetAllProfilesWithoutRole() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);
                Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg",
                                playerUser);
                profiles.save(playerProfile);

                User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER",
                                "Normal",
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
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);
                Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg",
                                playerUser);
                profiles.save(playerProfile);

                User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER",
                                "Normal",
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
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);
                Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg",
                                playerUser);
                profiles.save(playerProfile);

                User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER",
                                "Normal",
                                true);
                users.save(organizerUser);
                Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                                "images/organizer1.jpg", "Organization", organizerUser);
                profiles.save(organizerProfile);

                User organizerUser2 = new User("fang@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER",
                                "Normal",
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
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);
                Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg",
                                playerUser);
                profiles.save(playerProfile);

                User organizerUser = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER",
                                "Normal",
                                true);
                users.save(organizerUser);
                Profile organizerProfile = new Profile("Jane", "Smith", LocalDate.of(1985, 5, 15), "Los Angeles",
                                "images/organizer1.jpg", "Organization", organizerUser);
                profiles.save(organizerProfile);

                // Save another user for additional test case
                User anotherUser = new User("alice@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
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
                assertEquals(playerProfile.getBirthlocation(), profile.getBirthlocation(),
                                "Birth location should match");
                assertEquals(playerProfile.getProfilephotopath(), profile.getProfilephotopath(),
                                "Profile photo should match");
        }

        @Test
        public void testGetProfileThrowsUserNotFoundException() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);
                Profile playerProfile = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser);
                profiles.save(playerProfile);

                // We will attempt to get a profile for a non-existent user (with a non-existing
                // userId)
                Long nonExistentUserId = 999L;

                // Act & Assert: Verify that the exception is thrown when a profile is requested
                // for a non-existent user
                URI uri = new URI(baseUrl + port + "/profile/" + nonExistentUserId);

                ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

                assertEquals(404, result.getStatusCode().value());
        }

        @Test
        public void testGetProfileThrowsUserProfileNotFoundException() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser);

                // Act & Assert: Verify that the exception is thrown when a profile is requested
                // for a non-existent user
                URI uri = new URI(baseUrl + port + "/profile/" + playerUser.getId());

                ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

                assertEquals(404, result.getStatusCode().value());
        }

        @Test
        public void testGetLeaderboardReturnsSortedProfiles() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser1 = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser1);
                Profile playerProfile1 = new Profile("glenn", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser1, 2, 1, 6, 4, 1800);
                profiles.save(playerProfile1);

                User playerUser2 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser2);
                Profile playerProfile2 = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser2, 2, 2, 6, 6, 2000);
                profiles.save(playerProfile2);

                User playerUser3 = new User("kelvin@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser3);
                Profile playerProfile3 = new Profile("kelvin", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser3, 2, 0, 0, 0, 1200);
                profiles.save(playerProfile3);

                // Act: Call the endpoint
                URI uri = new URI(baseUrl + port + "/leaderboard");
                ResponseEntity<Profile[]> response = restTemplate.getForEntity(uri, Profile[].class);
                Profile[] profiles = response.getBody();

                // Assert: Verify the response status and content
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(profiles);
                // Verify that the leaderboard is sorted and has expected profiles
                assertEquals(3, profiles.length);
                // Additional checks to ensure the profiles are sorted as expected
                assertEquals("John", profiles[0].getFirstname());
                assertEquals("glenn", profiles[1].getFirstname());
                assertEquals("kelvin", profiles[2].getFirstname());
        }

        @Test
        public void testGetLeaderboardReturnsEmpty() throws Exception {
                // Arrange:

                // Act: Call the endpoint
                URI uri = new URI(baseUrl + port + "/leaderboard");
                ResponseEntity<Profile[]> response = restTemplate.getForEntity(uri, Profile[].class);
                Profile[] profiles = response.getBody();

                // Assert: Verify the response status and content
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(profiles);
                // Verify that the leaderboard is sorted and has expected profiles
                assertEquals(0, profiles.length);
        }

        @Test
        public void testGetUserRank_Success() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser1 = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser1);
                Profile playerProfile1 = new Profile("glenn", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser1, 2, 1, 6, 4, 1800);
                profiles.save(playerProfile1);

                User playerUser2 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser2);
                Profile playerProfile2 = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser2, 2, 2, 6, 6, 2000);
                profiles.save(playerProfile2);

                User playerUser3 = new User("kelvin@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser3);
                Profile playerProfile3 = new Profile("kelvin", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser3, 2, 0, 0, 0, 1200);
                profiles.save(playerProfile3);

                // Act: Call the correct "/playerrank" endpoint
                URI uri = new URI(baseUrl + port + "/playerrank");
                ResponseEntity<Map<Long, Integer>> response = restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<Map<Long, Integer>>() {
                                });
                Map<Long, Integer> ranks = response.getBody();

                // Assert: Verify the response status and content
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(ranks);
                assertEquals(3, ranks.size());

                // Verify the expected ranks
                assertEquals(2, ranks.get(playerUser1.getId()));
                assertEquals(1, ranks.get(playerUser2.getId()));
                assertEquals(3, ranks.get(playerUser3.getId()));
        }

        @Test
        public void testGetFullName_Success() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser1 = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser1);
                Profile playerProfile1 = new Profile("glenn", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser1, 2, 1, 6, 4, 1800);
                profiles.save(playerProfile1);

                User playerUser2 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser2);
                Profile playerProfile2 = new Profile("John", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser2, 2, 2, 6, 6, 2000);
                profiles.save(playerProfile2);

                // Act: Call the correct "/playerrank" endpoint
                URI uri = new URI(baseUrl + port + "/fullname/" + playerUser2.getId());
                ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
                String fullname = response.getBody();

                // Assert: Verify the response status and content
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(fullname);

                // Verify the expected fullname
                assertEquals("John Doe", fullname);
        }

        @Test
        public void testGetFullName_UserDoesNotExist() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser1 = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser1);
                Profile playerProfile1 = new Profile("glenn", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser1, 2, 1, 6, 4, 1800);
                profiles.save(playerProfile1);

                // We will attempt to get a profile for a non-existent user (with a non-existing
                // userId)
                Long nonExistentUserId = 999L;

                // Act: Call the correct "/playerrank" endpoint
                URI uri = new URI(baseUrl + port + "/fullname/" + nonExistentUserId);
                ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

                // Assert: Verify the response status and content
                assertEquals(404, response.getStatusCode().value());
        }

        /**
         * Creates HttpHeaders with Content-Type set to application/json.
         * Used for the JSON profile part of the request.
         */
        private HttpHeaders createJsonHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
        }

        /**
         * Creates HttpHeaders with Content-Type set to image/jpeg.
         * Used for the profile photo part of the request.
         */
        private HttpHeaders createImageHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                return headers;
        }

        @Test
        public void testPostProfile_UserExists_ProfileCreated() throws Exception {
                // Arrange: Create a test user with specified credentials and role
                User playerUser = new User("jane.doe@example.com", encoder.encode("securepassword"), "ROLE_PLAYER",
                                "Normal", true);
                users.save(playerUser); // Save the user to the database
                Long userId = playerUser.getId(); // Get the generated user ID

                // Prepare profile data as JSON
                Profile profileData = new Profile("Jane", "Doe", LocalDate.of(1995, 5, 20), "Los Angeles");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule to handle LocalDate
                                                                   // serialization
                String profileJson = objectMapper.writeValueAsString(profileData); // Convert profile data to JSON
                                                                                   // string

                // Prepare a mock profile photo file as ByteArrayResource for multipart request
                MockMultipartFile profilePhoto = new MockMultipartFile(
                                "profilePhoto",
                                "jane_doe.jpg",
                                "image/jpeg",
                                new byte[] { 1, 2, 3, 4 });

                // Wrap the photo bytes in a ByteArrayResource for the request body
                ByteArrayResource photoResource = new ByteArrayResource(profilePhoto.getBytes()) {
                        @Override
                        public String getFilename() {
                                return profilePhoto.getOriginalFilename();
                        }
                };

                // Act: Make a POST request to the profile creation endpoint with the user ID
                URI uri = new URI(baseUrl + port + "/user/" + userId + "/profile");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA); // Set content type to multipart/form-data
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // Accept JSON response

                // Prepare the request body with both JSON profile data and the profile photo
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("profile", new HttpEntity<>(profileJson, createJsonHeaders())); // Add profile JSON part
                body.add("profilePhoto", new HttpEntity<>(photoResource, createImageHeaders())); // Add profile photo
                                                                                                 // part

                // Create the request entity containing the headers and body
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                // Send the request with basic authentication and capture the response
                ResponseEntity<Profile> response = restTemplate
                                .withBasicAuth("jane.doe@example.com", "securepassword")
                                .postForEntity(uri, requestEntity, Profile.class);

                // Assert: Check that the profile was created successfully and verify its
                // properties
                assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Check for HTTP 201 Created status
                Profile createdProfile = response.getBody(); // Get the created profile from the response body
                assertNotNull(createdProfile); // Ensure the profile is not null
                assertEquals("Jane", createdProfile.getFirstname()); // Check firstname
                assertEquals("Doe", createdProfile.getLastname()); // Check lastname
                assertEquals(LocalDate.of(1995, 5, 20), createdProfile.getBirthdate()); // Check birthdate
                assertEquals("Los Angeles", createdProfile.getBirthlocation()); // Check birth location
                assertEquals("ProfilePhoto_" + userId + ".jpg", createdProfile.getProfilephotopath()); // Check profile
                                                                                                       // photo path
                assertEquals(0, createdProfile.getTournamentCount()); // Check tournament count
                assertEquals(0, createdProfile.getTournamentWinCount()); // Check tournament win count
                assertEquals(0, createdProfile.getMatchCount()); // Check match count
                assertEquals(0, createdProfile.getMatchWinCount()); // Check match win count
                assertEquals(1200, createdProfile.getPoints()); // Check default points
        }

        @Test
        public void testPutExistingProfile_ProfileUpdated() throws Exception {
                // Arrange: Create a test user and initial profile
                User playerUser = new User("jane.doe@example.com", encoder.encode("securepassword"), "ROLE_PLAYER",
                                "Normal", true);
                users.save(playerUser);
                Long userId = playerUser.getId();

                Profile initialProfile = new Profile("Jane", "Doe", LocalDate.of(1995, 5, 20), "Los Angeles");
                initialProfile.setUser(playerUser); // Link the profile to the user
                profiles.save(initialProfile); // Save the initial profile to the database

                // Prepare updated profile data as JSON
                Profile updatedProfileData = new Profile("Janet", "Doe", LocalDate.of(1996, 6, 15), "New York");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule for LocalDate support
                String updatedProfileJson = objectMapper.writeValueAsString(updatedProfileData);

                // Prepare new profile photo as a ByteArrayResource
                MockMultipartFile newProfilePhoto = new MockMultipartFile(
                                "profilePhoto",
                                "janet_doe.jpg",
                                "image/jpeg",
                                new byte[] { 5, 6, 7, 8 });

                ByteArrayResource photoResource = new ByteArrayResource(newProfilePhoto.getBytes()) {
                        @Override
                        public String getFilename() {
                                return newProfilePhoto.getOriginalFilename();
                        }
                };

                // Act: Make a PUT request to update the profile
                URI uri = new URI(baseUrl + port + "/user/" + userId + "/profile");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // Prepare the request body with updated JSON profile data and new profile photo
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("profile", new HttpEntity<>(updatedProfileJson, createJsonHeaders())); // Set JSON content type
                body.add("profilePhoto", new HttpEntity<>(photoResource, createImageHeaders())); // Set image content
                                                                                                 // type

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<Profile> response = restTemplate
                                .withBasicAuth("jane.doe@example.com", "securepassword")
                                .exchange(uri, HttpMethod.PUT, requestEntity, Profile.class);

                // Assert: Check that the profile was updated successfully
                assertEquals(HttpStatus.OK, response.getStatusCode());
                Profile updatedProfile = response.getBody();
                assertNotNull(updatedProfile);
                assertEquals("Janet", updatedProfile.getFirstname());
                assertEquals("Doe", updatedProfile.getLastname());
                assertEquals(LocalDate.of(1996, 6, 15), updatedProfile.getBirthdate());
                assertEquals("New York", updatedProfile.getBirthlocation());
                assertEquals("ProfilePhoto_" + userId + ".jpg", updatedProfile.getProfilephotopath());
        }

        @Test
        public void testPutExistingProfile_OnlyPhotoUpdated() throws Exception {
                // Arrange: Create a test user and initial profile
                User playerUser = new User("jane.doe@example.com", encoder.encode("securepassword"), "ROLE_PLAYER",
                                "Normal", true);
                users.save(playerUser);
                Long userId = playerUser.getId();

                // Initial profile data
                Profile initialProfile = new Profile("Jane", "Doe", LocalDate.of(1995, 5, 20), "Los Angeles",
                                "ProfilePhoto_123.jpg", playerUser);
                profiles.save(initialProfile); // Save the initial profile to the database

                // Prepare the same profile data as JSON (no changes to profile fields)
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule for LocalDate support
                String profileJson = objectMapper.writeValueAsString(initialProfile);

                // Prepare new profile photo as a ByteArrayResource
                MockMultipartFile newProfilePhoto = new MockMultipartFile(
                                "profilePhoto",
                                "new_photo.jpg",
                                "image/jpeg",
                                new byte[] { 9, 10, 11, 12 });

                ByteArrayResource photoResource = new ByteArrayResource(newProfilePhoto.getBytes()) {
                        @Override
                        public String getFilename() {
                                return newProfilePhoto.getOriginalFilename();
                        }
                };

                // Act: Make a PUT request to update only the profile photo
                URI uri = new URI(baseUrl + port + "/user/" + userId + "/profile");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // Prepare the request body with the unchanged JSON profile data and new profile
                // photo
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("profile", new HttpEntity<>(profileJson, createJsonHeaders())); // Set JSON content type
                body.add("profilePhoto", new HttpEntity<>(photoResource, createImageHeaders())); // Set image content
                                                                                                 // type

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<Profile> response = restTemplate
                                .withBasicAuth("jane.doe@example.com", "securepassword")
                                .exchange(uri, HttpMethod.PUT, requestEntity, Profile.class);

                // Assert: Check that the profile photo was updated successfully, while other
                // fields remain the same
                assertEquals(HttpStatus.OK, response.getStatusCode());
                Profile updatedProfile = response.getBody();
                assertNotNull(updatedProfile);
                assertEquals("Jane", updatedProfile.getFirstname()); // Ensure other fields are unchanged
                assertEquals("Doe", updatedProfile.getLastname());
                assertEquals(LocalDate.of(1995, 5, 20), updatedProfile.getBirthdate());
                assertEquals("Los Angeles", updatedProfile.getBirthlocation());
                assertEquals("ProfilePhoto_" + userId + ".jpg", updatedProfile.getProfilephotopath()); // Check new
                                                                                                       // photo path
        }
        
        @Test
        public void testChangePlayerStats_MatchNotFound() throws Exception {
                // Arrange: Create test users and profiles
                User playerUser1 = new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal",
                                true);
                users.save(playerUser1);
                User playerUser2 = new User("john@gmail.com", encoder.encode("anotherpassword"), "ROLE_PLAYER",
                                "Normal", true);
                users.save(playerUser2);

                Profile playerProfile1 = new Profile("Glenn", "Doe", LocalDate.of(1990, 1, 1), "New York",
                                "images/player1.jpg", playerUser1, 0, 0, 0, 0, 1200);
                profiles.save(playerProfile1);
                Profile playerProfile2 = new Profile("John", "Smith", LocalDate.of(1992, 3, 15), "Los Angeles",
                                "images/player2.jpg", playerUser2, 0, 0, 0, 0, 1200);
                profiles.save(playerProfile2);

                List<Long> players = new ArrayList<>();
                players.add(playerUser1.getId());
                players.add(playerUser2.getId());
                Tournament tournament = new Tournament("Tournament", "Singapore", LocalDate.of(2020, 1, 16), LocalDate.of(2020,1,30), LocalTime.of(13, 30), Tournament.Status.ONGOING, "description", playerUser1.getId(), players);
                tournaments.save(tournament);

                Match match1 = new Match (tournament, playerUser1, playerUser2, LocalDate.of(2020,1,16), LocalTime.of(13,30,00), 1, 0, Tournament.Status.ONGOING);
                matches.save(match1);


                // Create a match, assuming a match is created with two users
                Long matchId = match1.getId();
                Long winnerId = playerUser1.getId(); // Assume player 1 wins

                // Act: Call the correct "/playerstats/{matchId}/{winnerId}" endpoint
                URI uri = new URI(baseUrl + port + "/playerstats/" + matchId + "/" + winnerId);
                ResponseEntity<List<Profile>> response = restTemplate.exchange(uri, HttpMethod.PUT, null, new ParameterizedTypeReference<List<Profile>>() {});

                // Assert: Verify the response status and that player statistics are updated
                assertEquals(200, response.getStatusCode().value());
                List<Profile> updatedProfiles = response.getBody();
                assertNotNull(updatedProfiles);
                assertTrue(updatedProfiles.size() > 0);

                // Check if player statistics have been updated for both players
                Profile updatedProfile1 = updatedProfiles.get(0);
                Profile updatedProfile2 = updatedProfiles.get(1);

                assertEquals(1216, updatedProfile1.getPoints());
                assertEquals(1184, updatedProfile2.getPoints());
                assertEquals(1, updatedProfile1.getMatchWinCount());
                assertEquals(0, updatedProfile2.getMatchWinCount());
        }

}
