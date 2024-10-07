package csd.cuemaster.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.tournament.Tournament;

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
     * Returns a sorted list of players with given id.
     * @param URI
     * @param id
     * @return
     */
    public List<Profile> getLeaderboard(final String URI, final Long id) {
        ResponseEntity<List<Profile>> response = template.exchange(
        URI + "/" + id,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<Profile>>() {}
        );
        List<Profile> profiles = response.getBody();
        return profiles;
    }

    /**
     * Returns a list of players after a points reset with given id.
     * @param URI
     * @param id
     * @return
     */
    public List<Profile> resetPoints(final String URI, final Long id) {
        ResponseEntity<List<Profile>> response = template.exchange(
        URI + "/" + id,
        HttpMethod.PUT,
        null,
        new ParameterizedTypeReference<List<Profile>>() {}
        );
        List<Profile> profiles = response.getBody();
        return profiles;
    }

    /**
     * Create a new tournament.
     * 
     * @param URI The endpoint URI
     * @param newTournament The Tournament object to create
     * @return The created Tournament object
     */
    public Tournament createTournament(final String URI, final Tournament newTournament) {
        return template.postForObject(URI, newTournament, Tournament.class);
    }

    /**
     * Retrieve a tournament by ID.
     * 
     * @param URI The endpoint URI
     * @param id The ID of the tournament to retrieve
     * @return The retrieved Tournament object
     */
    public Tournament getTournamentById(final String URI, final Long id) {
        return template.getForObject(URI + "/" + id, Tournament.class);
    }

    /**
     * Retrieve all tournaments.
     * 
     * @param URI The endpoint URI
     * @return List of Tournament objects
     */
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
}