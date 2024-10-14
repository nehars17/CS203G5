package csd.cuemaster.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Arrays;

import csd.cuemaster.match.Match;
import csd.cuemaster.profile.Profile;
import csd.cuemaster.tournament.Tournament;

@Component
public class RestTemplateClient {
    private final RestTemplate template;

    /**
    //  * Add authentication information for the RestTemplate
    //  */
    public RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
        this.template = restTemplateBuilder
                .basicAuthentication("admin", "goodpassword")
                .build();
    }

    // /**
    //  * Returns a sorted list of players with given id.
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

    /**
     * Create a new tournament.
     * 
     * @param URI The endpoint URI
     * @param newTournament The Tournament object to create
     * @return The created Tournament object
     */
    public Tournament createTournament(final String URI, final Tournament newTournament) {
        final Tournament returned = template.postForObject(URI, newTournament, Tournament.class);
        
        return returned;
    }

    /**
     * Retrieve a tournament by ID.
     * 
     * @param URI The endpoint URI
     * @param id The ID of the tournament to retrieve
     * @return The retrieved Tournament object
     */
    public Tournament getTournamentById(final String URI, final Long id) {
        final Tournament tournament = template.getForObject(URI + "/" + id, Tournament.class);
        return tournament;
    }

    /**
     * Retrieve all tournaments.
     * 
     * @param URI The endpoint URI
     * @return List of Tournament objects
     */
    @SuppressWarnings("unchecked")
    public List<Tournament> getAllTournaments(final String URI) {
        return template.getForObject(URI, List.class);
    }

    /**
     * Update an existing tournament.
     * 
     * @param URI The endpoint URI
     * @param id The ID of the tournament to update
     * @param updatedTournament The updated Tournament object
     * @return The updated Tournament object
     */
    public Tournament updateTournament(final String URI, final Long id, final Tournament updatedTournament) {
        HttpEntity<Tournament> requestEntity = new HttpEntity<>(updatedTournament);
        ResponseEntity<Tournament> responseEntity = template.exchange(
                URI + "/" + id, HttpMethod.PUT, requestEntity, Tournament.class);
        return responseEntity.getBody();
    }

        //update match by id
        public Match updateMatch(final String URI, Long matchId, Match match) {
            template.put(URI + "/" + matchId, match);
            return getMatchById(URI, matchId);
        }
    /**
     * Delete a tournament by ID.
     * 
     * @param URI The endpoint URI
     * @param id The ID of the tournament to delete
     * @return ResponseEntity indicating the result of the operation
     */
    public ResponseEntity<Void> deleteTournament(final String URI, final Long id) {
        return template.exchange(URI + "/" + id, HttpMethod.DELETE, null, Void.class);
    }
    // /**
    //  * Returns a list of players after a points reset with given id.
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    // public List<Profile> resetPoints(final String URI, final Long id) {
    //     ResponseEntity<List<Profile>> response = template.exchange(
    //     URI + "/" + id,
    //     HttpMethod.PUT,
    //     null,
    //     new ParameterizedTypeReference<List<Profile>>() {}
    //     );
    //     List<Profile> profiles = response.getBody();
    //     return profiles;
    // }

        public Match createMatch(final String URI, Match match) {
        ResponseEntity<Match> response = template.postForEntity(URI + "/create", match, Match.class);
        return response.getBody();
    }

    //get match by id
    public Match getMatchById(final String URI, Long matchId) {
        return template.getForObject(URI + "/" + matchId, Match.class);
    }



    public List<Match> getAllMatchesByTournamentId(final String URI, Long tournamentId) {
        ResponseEntity<List> response = template.exchange(URI + "/tournaments/" + tournamentId, HttpMethod.GET, null, List.class
        );
        return response.getBody();
    }

    //delete match by ID
    public void deleteMatchById(final String URI, Long matchId) {
        template.delete(URI + "/" + matchId);
    }
}