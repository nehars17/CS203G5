package csd.cuemaster.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import csd.cuemaster.profile.Profile;

@Component
public class RestTemplateClient {
    private final RestTemplate template;

    /**
     * Add authentication information for the RestTemplate
     */
    public RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
        this.template = restTemplateBuilder
                .basicAuthentication("admin", "goodpassword")
                .build();
    }

    /**
     * Get a profile with given id
     * 
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    public Profile getProfileByProfileID(final String URI, final Long userid, final Long profileid) {
        final Profile profile = template.getForObject(URI + "/users/" + userid + "/profile/" + profileid, Profile.class);
        return profile;
    }

    /**
     * Get all profile
     * 
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    public List<Profile> getAllProfile(final String URI, final Long id) {
        Profile[] profileArray = template.getForObject(URI + "/profile", Profile[].class);
        List<Profile> profileList = Arrays.asList(profileArray);
        return profileList;
    }

    /**
     * Get update user profile
     * 
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    public Profile putUserProfile(final String URI, final Long userid, final Profile newProfile){

        HttpEntity<Profile> requestEntity = new HttpEntity<>(newProfile);
        ResponseEntity<Profile> responseEntity = template.exchange(URI + "/user/" + userid + "/profile/edit", HttpMethod.PUT, requestEntity, Profile.class);
        return responseEntity.getBody();
    }

    /**
     * Returns a sorted list of players with given id.
     * @param URI
     * @param id
     * @return
     */
    public List<Profile> getLeaderboard(final String URI, final Long id) {
        Profile[] profileArray = template.getForObject(URI + "/profile", Profile[].class);
        List<Profile> profileList = Arrays.asList(profileArray);
        return profileList;
    }

        /**
     * Get a profile with given id
     * 
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    public Profile changePoints(final String URI, final Long userid, final Long profileid) {
        final Profile profile = template.getForObject(URI + "/users/" + userid + "/profile/" + profileid, Profile.class);
        return profile;
    }
}