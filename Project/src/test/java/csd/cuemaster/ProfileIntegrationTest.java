package csd.cuemaster;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
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
    void tearDown(){
        profiles.deleteAll();
        users.deleteAll();
    }

    //BRYAN TESTS 
    @Test
    public void getProfile_2Profiles() throws Exception{
        URI uri = new URI(baseUrl + port + "/profiles");
        User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
        User user2 = users.save(new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal", true));
        profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1));                //Player Profile 
        profiles.save(new Profile("Glenn", "Fan", LocalDate.parse("2002-01-01"), "Singapore", "SMU",user2));        //Organizer Profile

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

		assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
		assertEquals(2, profiles.length);
	}

	@Test
	public void getProfile_Empty() throws Exception{
        URI uri = new URI(baseUrl + port + "/profiles");

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
		Profile[] profiles = result.getBody();

		assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
		assertEquals(0, profiles.length);
	}

    @Test 
    public void getProfile_ValidUserIdProfileId_Success() throws Exception{
        User user1 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        Long user_id = users.save(user1).getId();
        Profile profile = new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1); 
        Long profile_id = profiles.save(profile).getId();

        URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile/" + profile_id);

        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);
        Profile profileresult = result.getBody();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(profileresult, "Profile should not be null");
        assertEquals(profile.getId(), profileresult.getId());
        assertEquals(profile.getFirstname(), profileresult.getFirstname());
    }
    
    @Test
    public void getProfile_InvalidProfileId_Failure() throws Exception{
        User user1 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        Long user_id = users.save(user1).getId();

        URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile/1");

        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void getProfile_InvalidUserId_Failure() throws Exception{
        URI uri = new URI(baseUrl + port + "/user/2/profile/1");

        ResponseEntity<Profile> result = restTemplate.getForEntity(uri, Profile.class);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void postProfile_ValidUserId_Success() throws Exception{
        User user1 = new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true);
        Long user_id = users.save(user1).getId();
        Profile newprofile = new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1);

        URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile");
        
        ResponseEntity<Profile> result = restTemplate.withBasicAuth("bryan@gmail.com","goodpassword")
                                                    .postForEntity(uri, newprofile, Profile.class);
        Profile profileresult = result.getBody();

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(profileresult, "Profile should not be null");
		assertEquals(newprofile.getFirstname(), profileresult.getFirstname());
        assertEquals(newprofile.getLastname(), profileresult.getLastname());
        assertEquals(newprofile.getBirthdate(), profileresult.getBirthdate());
        assertEquals(newprofile.getBirthlocation(), profileresult.getBirthlocation());
        assertEquals(newprofile.getUser().getId(), profileresult.getUser().getId());
    }

    @Test
    public void postProfile_InvalidUserId_Failure() throws Exception{
        users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
        Long user_id = 10L; 
        Profile newprofile = new Profile("Glenn", "Fan", LocalDate.parse("2002-01-01"), "Singapore",null);
       
        URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile");

        ResponseEntity<Profile> result = restTemplate.withBasicAuth("bryan@gmail.com","goodpassword")
                                                .postForEntity(uri, newprofile, Profile.class);

        assertEquals(404, result.getStatusCode().value());
    }

    // @Test 
    // public void postProfile_ProfileCreatedAlready_Failure() throws Exception{
    //     User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
    //     Long user_id = user1.getId();
    //     Profile profile = profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1));

    //     URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile");

    //     Profile newprofile = new Profile("Glenn", "Fan", LocalDate.parse("2000-01-01"), "Singapore");

    //     ResponseEntity<Profile> result = restTemplate.withBasicAuth("bryan@gmail.com","goodpassword")
    //                                             .exchange(baseUrl + port + "/user/" + user_id + "/profile",HttpMethod.POST, new HttpEntity<>(newprofile),Profile.class);

    //     System.out.println(user1 .getProfile());
    //     System.out.println("Response status: " + result.getStatusCode().value());
    //     System.out.println("Response body: " + result.getBody()); 

    //     assertEquals(400, result.getStatusCode().value());
    // }

    // @Test
    // public void putProfile_Success() throws Exception{
    //     User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
    //     Long user_id = user1.getId(); 
    //     Profile profile = profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1));

    //     Profile newprofileinfo = new Profile("pagee", "Tan", LocalDate.parse("1999-01-01"), "Australia",user1);

    //     URI uri = new URI(baseUrl + port + "/user/" + user_id + "/profile/edit");

    //     ResponseEntity<Profile> result = restTemplate.withBasicAuth("bryan@gmail.com","goodpassword")
    //                                             .exchange(uri,HttpMethod.PUT, new HttpEntity<>(newprofileinfo), Profile.class);
    //     Profile profileresult = result.getBody();
    //     System.out.println(profile);
    //     System.out.println(user_id);
    //     System.out.println(user1.getProfile());

    //     assertEquals(200, result.getStatusCode().value());
    //     assertNotNull(profileresult, "Profile should not be null");
    //     assertEquals(newprofileinfo.getFirstname(), profileresult.getFirstname());
    //     assertEquals(newprofileinfo.getLastname(), profileresult.getLastname());
    //     assertEquals(newprofileinfo.getBirthdate(), profileresult.getBirthdate());
    //     assertEquals(newprofileinfo.getBirthlocation(), profileresult.getBirthlocation());
    //     assertEquals(newprofileinfo.getUser(), profileresult.getUser());
    // }

    @Test
    public void getLeaderboard_1Player_Success() throws Exception{
        URI uri = new URI(baseUrl + port + "/leaderboard");
        User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
        User user2 = users.save(new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal", true));
        profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1));                //Player Profile 
        profiles.save(new Profile("Glenn", "Fan", LocalDate.parse("2002-01-01"), "Singapore", "SMU",user2));        //Organizer Profile

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

		assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
		assertEquals(1, profiles.length);
	}

    @Test
    public void getLeaderboard_Empty_Success() throws Exception{
        URI uri = new URI(baseUrl + port + "/leaderboard");
        User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal", true));
        User user2 = users.save(new User("glenn@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "Normal", true));
        profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore","SMU", user1));                //Player Profile 
        profiles.save(new Profile("Glenn", "Fan", LocalDate.parse("2002-01-01"), "Singapore", "SMU",user2));        //Organizer Profile

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
        Profile[] profiles = result.getBody();

		assertEquals(200, result.getStatusCode().value());
        assertNotNull(profiles, "Profiles should not be null");
		assertEquals(0, profiles.length);
	}

    // @Test
    // public void changePlayerPoint_Success() throws Exception{
    //     User admin = users.save(new User("admin@gmail.com", encoder.encode("goodpassword"), "ROLE_ADMIN", "Normal", true));
    //     User user1 = users.save(new User("bryan@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "Normal", true));
    //     Long user_id = user1.getId();

    //     profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore", user1));                //Player Profile 

    //     URI uri = new URI(baseUrl + port + "/changepoints/" + user_id);

    //     Profile newprofile = new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore", user1, 2300);

	// 	ResponseEntity<Profile> result = restTemplate.withBasicAuth("admin@gmail.com","goodpassword")
    //                                                 .exchange(uri,HttpMethod.PUT, new HttpEntity<>(newprofile), Profile.class);
    //     Profile profileresult = result.getBody();

	// 	assertEquals(200, result.getStatusCode().value());
    //     assertNotNull(profileresult, "Profiles should not be null");
    //     Profile updatedProfile = profiles.findByUserId(user_id).orElse(null); // Ensure you have a way to fetch the updated profile
    //         assertNotNull(updatedProfile, "Updated profile should not be null");

	// 	assertEquals(user1.getProfile().getPoints(), profileresult.getPoints());
	// }
}



