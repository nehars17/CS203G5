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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Controller for managing match-related operations within the application.
 * Provides endpoints to create, update, retrieve, and delete matches as well
 * as declare a winner for a match.
 */

@RestController
public class MatchController {
    
    @Autowired
    private MatchService matchService;


    /**
     * Creates matches for the given tournament based on the tournament ID.
     * This method is also used to call the match pairing for players participating in the specified tournament round ensuring that the two players are correctly allocated a match.
     *
     * @param tournamentId the ID of the tournament for which matches are to be created
     * @return ResponseEntity containing a list of created matches and an HTTP status of 201 (Created)
     */
    @PostMapping("/matches")
    public ResponseEntity<List<Match>> createMatchesFromTournament(@RequestParam Long tournamentId) {
        List<Match> createdMatches = matchService.createMatchesFromTournaments(tournamentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatches);
    }

    /**
     * Updates an existing match based on the provided match ID and match data.
     * The specified match is updated with the details from the provided Match object.
     *
     * @param matchId the ID of the match to be updated
     * @param match the Match object containing updated match data
     * @return ResponseEntity with a message indicating the match was updated and an HTTP status of 200 (OK)
     */
    @PutMapping("/matches/{matchId}")
    public ResponseEntity<String> updateMatch(@PathVariable Long matchId, @Valid @RequestBody Match match) {
        matchService.updateMatch(matchId, match);
        return ResponseEntity.ok("Match updated: id = " + matchId);
    }

        /**
     * Retrieves a match based on the provided match ID.
     * If the match does not exist, a ResourceNotFoundException is thrown.
     *
     * @param matchId the ID of the match to retrieve
     * @return ResponseEntity containing the Match object if found, otherwise throws ResourceNotFoundException
     * @throws ResourceNotFoundException if a match with the specified ID is not found
     */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long matchId) {
        Match savedMatch = matchService.getMatchById(matchId);
        if (savedMatch == null) {
            throw new ResourceNotFoundException("Match with ID " + matchId + " does not exist.");
        }
        return ResponseEntity.ok(savedMatch);
    }

    /**
     * Retrieves a list of all matches available in the system.
     *
     * @return ResponseEntity containing a list of all Match objects and an HTTP status of 200 (OK)
     */
    @GetMapping("/matchlist")
    public ResponseEntity<List<Match>> getAllMatches() {
        List<Match> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }
    
    /**
     * Deletes a match based on the provided match ID.
     * If the match does not exist, a ResourceNotFoundException is thrown.
     *
     * @param matchId the ID of the match to delete
     * @return ResponseEntity with an HTTP status of 204 (No Content) if deletion is successful
     */
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatchById(matchId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Declares a winner for the specified match.
     * This updates the match to record the winner based on the provided winner ID.
     *
     * @param matchId the ID of the match for which the winner is to be declared
     * @param winnerId the ID of the user who won the match
     * @return ResponseEntity containing a success message and an HTTP status of 200 (OK) if successful
     * @throws ResourceNotFoundException if either the match or the winner is not found
     * @throws IllegalArgumentException if any validation fails for declaring the winner
     */
    @PostMapping("/matches/{matchId}/winner/{winnerId}")
    public ResponseEntity<String> declareWinner(@PathVariable Long matchId, @PathVariable Long winnerId) {
        try {
            matchService.declareWinner(matchId, winnerId);
            return ResponseEntity.ok("Winner declared for match ID: " + matchId);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    /**
     * Handles exceptions of type ResourceNotFoundException, returning a NOT FOUND (404) response.
     *
     * @param ex the exception to handle
     * @return ResponseEntity containing the exception message and a 404 (Not Found) status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles exceptions of type IllegalArgumentException, returning a BAD REQUEST (400) response.
     *
     * @param ex the exception to handle
     * @return ResponseEntity containing the exception message and a 400 (Bad Request) status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
