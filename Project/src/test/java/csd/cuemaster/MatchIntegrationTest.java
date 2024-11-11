// package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.match.MatchService;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MatchIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProfileRepository profiles; 

    @Autowired
    private UserRepository users;

    @Autowired
    private TournamentRepository tournaments;
   
    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @AfterEach
    void tearDown(){
        matchRepository.deleteAll();
    }

    @Test
    public void createMatch_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/matches/");

        Match match = new Match();
        match.setRoundNumber(1);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);

        ResponseEntity<Match> result = restTemplate.postForEntity(uri, match, Match.class);

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody().getId());
        assertEquals(1L, result.getBody().getPlayer1Id());
    }

    @Test
    public void getMatchById_ValidId_Success() throws Exception {
        // Save a match to retrieve later
        Match match = new Match();
        match.setRoundNumber(1);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);
        Match savedMatch = matchRepository.save(match);

        URI uri = new URI(baseUrl + port + "/matches/" + savedMatch.getId());

        ResponseEntity<Match> result = restTemplate.getForEntity(uri, Match.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1L, result.getBody().getPlayer1Id());
    }

    @Test
    public void getMatchById_InvalidId_Failure() throws Exception {
        URI uri = new URI(baseUrl + port + "/matches/999");

        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void updateMatch_ValidId_Success() throws Exception {
        Match match = new Match();
        match.setRoundNumber(1);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);
        Match savedMatch = matchRepository.save(match);

        URI uri = new URI(baseUrl + port + "/matches/" + savedMatch.getId());

        Match updatedMatch = new Match();
        updatedMatch.setRoundNumber(2);
        updatedMatch.setPlayer1Id(1L);
        updatedMatch.setPlayer2Id(3L);

        ResponseEntity<Match> result = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(updatedMatch), Match.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(3L, result.getBody().getPlayer2Id());
    }

    @Test
    public void deleteMatch_ValidId_Success() throws Exception {
        Match match = new Match();
        match.setRoundNumber(1);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);
        Match savedMatch = matchRepository.save(match);

        URI uri = new URI(baseUrl + port + "/matches/" + savedMatch.getId());

        ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class);

        assertEquals(204, result.getStatusCode().value());

        // Check that the match is actually deleted
        ResponseEntity<String> getResult = restTemplate.getForEntity(uri, String.class);
        assertEquals(404, getResult.getStatusCode().value());
    }

    @Test
    public void declareWinner_ValidMatchAndWinner_Success() throws Exception {
        Match match = new Match();
        match.setRoundNumber(1);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);
        Match savedMatch = matchRepository.save(match);

        URI uri = new URI(baseUrl + port + "/matches/" + savedMatch.getId() + "/winner/1");

        ResponseEntity<Match> result = restTemplate.postForEntity(uri, null, Match.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1L, result.getBody().getWinnerId());
    }

    @Test
    public void declareWinner_InvalidMatchId_Failure() throws Exception {
        URI uri = new URI(baseUrl + port + "/matches/999/winner/1");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, null, String.class);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void createMatchesForTournament_Success() throws Exception {
        // Assume tournamentId 1L is valid and has associated players
        URI uri = new URI(baseUrl + port + "/matchmaking/1");

        ResponseEntity<Match[]> result = restTemplate.postForEntity(uri, null, Match[].class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(4, result.getBody().length);  // Adjust this as per your tournament's match creation logic
    }
}


