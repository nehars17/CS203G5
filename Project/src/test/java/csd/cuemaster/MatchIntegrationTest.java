package csd.cuemaster;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchService;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
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
    private TournamentRepository tournaments;
   
    @Autowired
    private MatchService matchService;

    private Match match;
    private Tournament tournament;
    private Profile user1;
    private Profile user2;
    
    @BeforeEach
    void setUp() {
        baseUrl = baseUrl + port;

        // Create and save sample entities
        tournament = tournaments.save(new Tournament());
        user1 = users.save(new Profile());
        user2 = users.save(new User());

        match = new Match();
        match.setTournament(tournament);
        match.setUser1(user1);
        match.setUser2(user2);
        match.setMatchDate(LocalDate.now());
        match.setMatchTime(LocalTime.now());
        match.setUser1Score(0);
        match.setUser2Score(0);

        match = matchService.createMatch(match); // Save the match using the actual service
    }

    @AfterEach
    void tearDown(){
        profiles.deleteAll();
        users.deleteAll();
        tournaments.deleteAll();
    }

    @Test
    void testCreateMatch() {
        Match newMatch = new Match();
        newMatch.setTournament(tournament);
        newMatch.setUser1(user1);
        newMatch.setUser2(user2);
        newMatch.setMatchDate(LocalDate.now());
        newMatch.setMatchTime(LocalTime.now());
        newMatch.setUser1Score(0);
        newMatch.setUser2Score(0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Match> request = new HttpEntity<>(newMatch, headers);

        ResponseEntity<Match> response = restTemplate.postForEntity(baseUrl + "/matches/create", request, Match.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testUpdateMatch() {
        match.setUser1Score(1);
        match.setUser2Score(2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Match> request = new HttpEntity<>(match, headers);

        ResponseEntity<Match> response = restTemplate.exchange(baseUrl + "/matches/" + match.getId(), HttpMethod.PUT, request, Match.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getUser1Score());
        assertEquals(2, response.getBody().getUser2Score());
    }

    @Test
    void testGetMatchById() {
        ResponseEntity<Match> response = restTemplate.getForEntity(baseUrl + "/matches/" + match.getId(), Match.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(match.getId(), response.getBody().getId());
    }

    @Test
    void testGetAllMatches() {
        ResponseEntity<Match[]> response = restTemplate.getForEntity(baseUrl + "/matches", Match[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
    }

    @Test
    void testDeleteMatch() {
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/matches/" + match.getId(), HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify deletion
        ResponseEntity<Match> deletedMatchResponse = restTemplate.getForEntity(baseUrl + "/matches/" + match.getId(), Match.class);
        assertEquals(HttpStatus.NOT_FOUND, deletedMatchResponse.getStatusCode());
    }

    @Test
    void testDeclareWinner() {
        Long winnerId = user1.getId();
        ResponseEntity<Match> response = restTemplate.postForEntity(baseUrl + "/matches/" + match.getId() + "/declareWinner/" + winnerId, null, Match.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(match.getId(), response.getBody().getId());
        assertEquals(winnerId, response.getBody().getWinner().getId());
    }
}

