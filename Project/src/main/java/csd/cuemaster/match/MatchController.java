package csd.cuemaster.match;

import java.util.List;

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
    // @PostMapping("/matches/create")
    // public Match createMatch(@Valid @RequestBody Match match) {
    //     return matchService.createMatch(match);     
    // }

    // //create match
    // @PostMapping("/matches/")
    // public ResponseEntity<String> createMatch(@Valid @RequestBody Match match) {
        
    //     Match createdMatch = matchService.createMatch(match);
    //     // return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    //     return ResponseEntity.ok("match created: id =" + createdMatch.getId());
    // }
    
    // Create matches for a tournament's next round
    @PostMapping("/matches/tournament/{tournamentId}/next-round")
    public ResponseEntity<List<Match>> createMatchesForNextRound(@PathVariable Long tournamentId) {
        try {
            List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(matches);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    //update match
    @PutMapping("/matches/{matchId}")
    public ResponseEntity<Match> updateMatch(@PathVariable Long matchId, @Valid @RequestBody Match match) {
        match.setId(matchId);
        //Match updatedMatch = 
        Match updatedMatch = matchService.updateMatch(matchId, match);
        return ResponseEntity.ok(updatedMatch);
    }

    //get match info by id
    @GetMapping("/matches/{matchId}")
    public Match getMatchById(@PathVariable Long matchId) {
        Match savedMatch = matchService.getMatchById(matchId);
        
        if (savedMatch == null) {
            throw new ResourceNotFoundException("match with this id does not exist:" + matchId);
        }
        
        return matchService.getMatchById(matchId);
    }

    //get all matches
    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getAllMatches() {
        List<Match> matches = matchService.getAllMatches();

        if (matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(matches); // 204 No Content for an empty list
        }

        return ResponseEntity.ok(matches);
    }
    

    // Get all matches for a specific tournament
    @GetMapping("/matches/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getMatchesByTournamentId(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournamentId(tournamentId);

        if (matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(matches); // 204 No Content for an empty list
        }

        return ResponseEntity.ok(matches);
    }

    //delete a match
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatchById(matchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/matches/{matchId}/winner/{winnerId}")
    public ResponseEntity<Match> declareWinner(@PathVariable Long matchId, @PathVariable Long winnerId) {
        try {
            Match updatedMatch = matchService.declareWinner(matchId, winnerId);
            return ResponseEntity.ok(updatedMatch);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // @PostMapping("/matchmaking/{tournamentId}")
    // public List<Match> createMatches(@PathVariable (value = "tournamentId") Long tournamentId) {
    //     return matchService.createMatchesFromTournaments(tournamentId);
    // }

    // Exceptions handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
    }
}
