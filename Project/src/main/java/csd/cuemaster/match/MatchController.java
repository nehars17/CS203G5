package csd.cuemaster.match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    
    @Autowired
    private MatchService matchService;

    //create or update match
    @PostMapping("/create-update")
    public ResponseEntity<Match> createUpdateMatch (@RequestBody Match match) {
        //TODO: process POST request
        Match savedMatch = matchService.saveMatch(match);
        return ResponseEntity.ok(savedMatch);
    }
    
    //get match info by id
    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long matchId) {
        Match savedMatch = matchService.getMatchById(matchId);
        return savedMatch != null ? ResponseEntity.ok(savedMatch) : ResponseEntity.notFound().build();
    }

    //get all matches per tournament
    //need edits
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getAllMatchesByTournamentId(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournamentId(tournamentId);
        return ResponseEntity.ok(matches);
    }

    //delete a match
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatchById(matchId);
        return ResponseEntity.noContent().build();
    }
}
