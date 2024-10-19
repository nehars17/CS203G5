package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

import java.util.*;


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

    @AfterEach
    void tearDown(){
        profiles.deleteAll();
        users.deleteAll();
    }

    //BRYAN TESTS 
    @Test
    public void getProfile_2Profiles() throws Exception {
        URI uri = new URI(baseUrl + port + "/profiles");
        User user1 = users.save(new User("bryan@gmail.com", "goodpassword", "ROLE_PLAYER", "Normal", true));
        User user2 = users.save(new User("glenn@gmail.com", "goodpassword", "ROLE_ORGANISER", "Normal", true));
        profiles.save(new Profile("Bryan", "Ng", LocalDate.parse("2000-01-01"), "Singapore",user1));                //Player Profile -- do I need to add in an USER Object??
        profiles.save(new Profile("Glenn", "Fan", LocalDate.parse("2002-01-01"), "Singapore", "SMU",user2));        //Organizer Profile

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
		Profile[] profile_array = result.getBody();

		assertEquals(200, result.getStatusCode().value());
		assertEquals(2, profile_array.length);
	}

	@Test
	public void getProfile_Empty() throws Exception {
        URI uri = new URI(baseUrl + port + "/profiles");

		ResponseEntity<Profile[]> result = restTemplate.getForEntity(uri, Profile[].class);
		Profile[] profile_array = result.getBody();

		assertEquals(200, result.getStatusCode().value());
		assertEquals(0, profile_array.length);
	}


//USER TEST
	@Test 
	public void getUser_Success() throws Exception {
		URI uri = new URI(baseUrl + port + "/users");
		users.save(new User("bryan@gmail.com", "goodpassword", "ROLE_PLAYER", "Normal", true));

		ResponseEntity<User[]> result = restTemplate.getForEntity(uri, User[].class);
		User[] user_array = result.getBody();
		
		assertEquals(200, result.getStatusCode().value());
		assertEquals(1, user_array.length);
	}
}



