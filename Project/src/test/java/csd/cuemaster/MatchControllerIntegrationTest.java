package csd.cuemaster;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.profile.Profile;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatchControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port + "/matches";
        // Clear repositories or set up sample data if necessary
        matchRepository.deleteAll();
        userRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Test
    public void testCreateMatchesFromTournament() {
        // Set up sample tournament and users
        Tournament tournament = new Tournament();
        tournament.setTournamentname("Test Tournament");
        tournamentRepository.save(tournament);

        User user1 = createUser("player1");
        User user2 = createUser("player2");
        tournament.setPlayers(List.of(user1.getProfile(), user2.getProfile()));
        tournamentRepository.save(tournament);

        // Send a POST request to create matches
        ResponseEntity<Match[]> response = restTemplate.postForEntity(
                baseUrl + "?tournamentId=" + tournament.getId(), null, Match[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);
        assertThat(response.getBody()[0].getTournament().getId()).isEqualTo(tournament.getId());
    }

    @Test
    public void testUpdateMatch() {
        // Create sample match and save it
        Match match = createSampleMatch();

        // Update the match details
        match.setUser1Score(5);
        HttpEntity<Match> requestUpdate = new HttpEntity<>(match);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + match.getId(), HttpMethod.PUT, requestUpdate, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Match updated: id = " + match.getId());
    }

    @Test
    public void testGetMatchById() {
        Match match = createSampleMatch();

        ResponseEntity<Match> response = restTemplate.getForEntity(
                baseUrl + "/" + match.getId(), Match.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(match.getId());
    }

    @Test
    public void testGetAllMatches() {
        createSampleMatch();
        createSampleMatch();

        ResponseEntity<Match[]> response = restTemplate.getForEntity(baseUrl + "/matchlist", Match[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testDeleteMatch() {
        Match match = createSampleMatch();

        restTemplate.delete(baseUrl + "/" + match.getId());

        ResponseEntity<Match> response = restTemplate.getForEntity(
                baseUrl + "/" + match.getId(), Match.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeclareWinner() {
        // Create sample match and users
        User winner = createUser("winner");
        Match match = createSampleMatch();
        match.setUser1(winner);
        matchRepository.save(match);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/" + match.getId() + "/winner/" + winner.getId(), null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Winner declared for match ID: " + match.getId());
    }

    // Helper methods to create sample data
    private User createUser(String username) {
        
        User user = new User();
        user.setUsername(username);

        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        
        profile.setId(1L);

        user.setProfile(profile);
        return userRepository.save(user);
    }

    private Match createSampleMatch() {
        Tournament tournament = new Tournament();
        tournament.setTournamentname("Test Tournament");
        tournamentRepository.save(tournament);

        User user1 = createUser("player1");
        User user2 = createUser("player2");

        Match match = new Match(tournament, user1, user2, LocalDate.now(), LocalTime.now(), 0, 0, "UPCOMING");
        return matchRepository.save(match);
    }
}
