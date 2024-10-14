package csd.cuemaster.match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;


@RestController
public class MatchController {
    
    @Autowired
    private MatchService matchService;

    //create match
    //@ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/matches/create")
    public Match createMatch(@Valid @RequestBody Match match) {
        return matchService.createMatch(match);
    }
    
    //update match
    @PutMapping("/matches/{matchId}")
    public Match updateMatch(@PathVariable Long id, @Valid @RequestBody Match match) {
        Match updatedMatch = matchService.updateMatch(id, match);
        if (updatedMatch == null) throw new MatchNotFoundException(id);

        return updatedMatch;
    }

    //get match info by id
    @GetMapping("/matches/{matchId}")
    public Match getMatchById(@PathVariable Long matchId) {
        Match savedMatch = matchService.getMatchById(matchId);
        
        if (savedMatch == null) {
            throw new MatchNotFoundException(matchId);
        }
        
        return matchService.getMatchById(matchId);
    }

    //get all matches per tournament
    //need edits
    // @GetMapping("/tournament/matches/{tournamentId}")
    // public List<Match> getAllMatchesByTournamentId(@PathVariable Long tournamentId) {
    //     List<Match> matches = matchService.getMatchesByTournamentId(tournamentId);

    //     return matchService.getMatchesByTournamentId(tournamentId);
    // }

    //delete a match
    @DeleteMapping("/matches/{matchId}")
    public void deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatchById(matchId);
    }

    //declare winner for a match
    @PostMapping("/matches/{matchId}/declareWinner/{winnerId}")
    public Match declareWinner(@PathVariable Long matchId, @PathVariable Long winnerId) {        
        return matchService.declareWinner(matchId, winnerId);
    }

    // Exceptions handler
    @ExceptionHandler(MatchNotFoundException.class)
    public ResponseEntity<String> handleMatchNotFound(MatchNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // @ExceptionHandler(IllegalArgumentException.class)
    // public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    // }


}
