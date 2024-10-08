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
     * 
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


    // /**
    //  * Add a new profile
    //  * 
    //  * @param URI
    //  * @param newBook
    //  * @return
    //  */
    public Profile addBook(final String URI, final Long userid, final Profile newProfile) {
        final Profile returned = template.postForObject(URI + "/users/" + userid + "/profile", newProfile, Profile.class);
        
        return returned;
    }



    // public Book getBook(final String URI, final Long id) {
    //     final Book book = template.getForObject(URI + "/" + id, Book.class);
    //     return book;
    // }

    // /**
    //  * Add a new book
    //  * 
    //  * @param URI
    //  * @param newBook
    //  * @return
    //  */
    // public Book addBook(final String URI, final Book newBook) {
    //     final Book returned = template.postForObject(URI, newBook, Book.class);
        
    //     return returned;
    // }

    // /**
    //  * Get a book, but return a HTTP response entity.
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    // public ResponseEntity<Book> getBookEntity(final String URI, final Long id){
    //     return template.getForEntity(URI + "/{id}", Book.class, Long.toString(id));
    // }
    
}