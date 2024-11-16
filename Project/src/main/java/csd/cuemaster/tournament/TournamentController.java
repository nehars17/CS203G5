package csd.cuemaster.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import csd.cuemaster.profile.ProfileService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private ProfileService profileService;

    /**
     * Add a new tournament with POST request to "/tournaments"
     * Note the use of @RequestBody
     * @param tournament
     * @return list of all tournaments
     */

    // Create a new Tournament
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/tournaments")
    public Tournament createTournament(@Valid @RequestBody Tournament tournament) {
        return tournamentService.createTournament(tournament);
    }

    /**
     * List all tournaments in the system
     * @return list of all tournaments
     */

    // Get all Tournaments
    @GetMapping("/tournaments")
    public List<Tournament> getAllTournaments() {
        return tournamentService.getAllTournaments();
    }

    /**
     * Search for tournament with the given id
     * If there is no tournament with the given "id", throw a TournamentNotFoundException
     * @param id
     * @return tournament with the given id
     */

    // Get a specific Tournament by ID
    @GetMapping("/tournaments/{id}")
    public Tournament getTournamentById(@PathVariable Long id) {
        Tournament tournament = tournamentService.getTournamentById(id);

        // Need to handle "book not found" error using proper HTTP status code
        // In this case it should be HTTP 404
        if(tournament == null) throw new TournamentNotFoundException(id);
        return tournamentService.getTournamentById(id);
    }

    /**
     * Search for tournament with the given id
     * If there is no tournament with the given "id", throw a TournamentNotFoundException
     * @param id
     * @return tournament with the given id
     */

    // Update a Tournament by ID
    @PutMapping("/tournaments/{id}")
    public Tournament updateTournament(@PathVariable Long id, @Valid @RequestBody Tournament tournamentDetails) {
        Tournament updatedTournament = tournamentService.updateTournament(id, tournamentDetails);
        if(updatedTournament == null) throw new TournamentNotFoundException(id);
        
        return updatedTournament;
    }

    /**
     * Remove a book with the DELETE request to "/books/{id}"
     * If there is no book with the given "id", throw a BookNotFoundException
     * @param id
     */

    // Delete a Tournament by ID
    @DeleteMapping("/tournaments/{id}")
    public void deleteTournament(@PathVariable Long id) {
        // try {
        tournamentService.deleteTournament(id);
        // } catch (EmptyResultDataAccessException e) { 
        //     throw new TournamentNotFoundException(id);
        // }
    }

    @PostMapping("/tournaments/{id}/join")
    public ResponseEntity<Tournament> joinTournament(@PathVariable Long id, @RequestBody Long playerId) {
        Tournament updatedTournament = tournamentService.joinTournament(id, playerId);
        profileService.increaseTournamentCount(playerId);
        return ResponseEntity.ok(updatedTournament);
    }

    @PostMapping("/tournaments/{id}/leave")
    public ResponseEntity<Tournament> leaveTournament(@PathVariable Long id, @RequestBody Long playerId) {
        Tournament updatedTournament = tournamentService.leaveTournament(id, playerId);
        profileService.decreaseTournamentCount(playerId);
        return ResponseEntity.ok(updatedTournament);
    }

    @PostMapping("/tournaments/{id}/set-winner")
    public ResponseEntity<?> setTournamentWinner(@PathVariable Long id, @RequestBody Long winnerId) {
        try {
            Tournament tournament = tournamentService.setWinner(id, winnerId);
            profileService.TournamentWinCount(winnerId); // Increment win count for winner
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to set tournament winner: " + e.getMessage());
        }
    }

    


}