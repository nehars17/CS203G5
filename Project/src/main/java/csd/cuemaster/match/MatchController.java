package csd.cuemaster.match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // //create match
    // @PostMapping("/matches/")
    // public ResponseEntity<String> createMatch(@Valid @RequestBody Match match) {
        
    //     Match createdMatch = matchService.createMatch(match);
    //     return ResponseEntity.ok("match created: id =" + createdMatch.getId());
    // }

    // Create a match
    @PostMapping("/matches/")
    public ResponseEntity<String> createMatch(@Valid @RequestBody Match match) {
        Match createdMatch = matchService.createMatchesFromTournaments(tournamentId)(match);
        return ResponseEntity.status(HttpStatus.CREATED).body("Match created: id = " + createdMatch.getId());
    }

    /**
     * @param matchId
     * @param match
     * @return 
     */
    @PutMapping("/matches/{matchId}")
    public ResponseEntity<String> updateMatch(@PathVariable Long matchId, @Valid @RequestBody Match match) {
        match.setId(matchId);
        matchService.updateMatch(matchId, match);
        return ResponseEntity.ok("match updated: id =" + matchId);
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

    //tempo: get all matches
    @GetMapping("/matchlist")
    public List<Match> getAllMatches() {
        return matchService.getAllMatches();
    }
    
    //delete a match
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatchById(matchId);
        return ResponseEntity.noContent().build();
    }


    //need fixing
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

    @PostMapping("/matchmaking/{tournamentId}")
    public List<Match> createMatches(@PathVariable (value = "tournamentId") Long tournamentId) {
        return matchService.createMatchesFromTournaments(tournamentId);
    }

    // // Exceptions handler
    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    // }

    // @ExceptionHandler(IllegalArgumentException.class)
    // public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    // }


}
