package csd.cuemaster.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import csd.cuemaster.match.Match;

public class MatchClient {
    private final String BASE_URL = "http://localhost:8080/matches";

    private final RestTemplate restTemplate;

    @Autowired
    public MatchClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //create new match
    public Match createMatch(Match match) {
        ResponseEntity<Match> response = restTemplate.postForEntity(BASE_URL + "/create", match, Match.class);
        return response.getBody();
    }

    //get match by id
    public Match getMatchById(Long matchId) {
        return restTemplate.getForObject(BASE_URL + "/" + matchId, Match.class);
    }

    //update match by id
    public Match updateMatch(Long matchId, Match match) {
        restTemplate.put(BASE_URL + "/" + matchId, match);
        return getMatchById(matchId);
    }

    public List<Match> getAllMatchesByTournamentId(Long tournamentId) {
        ResponseEntity<List> response = restTemplate.exchange(BASE_URL + "/tournament/" + tournamentId, HttpMethod.GET, null, List.class
        );
        return response.getBody();
    }

    //delete match by ID
    public void deleteMatchById(Long matchId) {
        restTemplate.delete(BASE_URL + "/" + matchId);
    }

}


