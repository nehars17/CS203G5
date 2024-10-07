package csd.cuemaster.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
// @RequestMapping("/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    // Create a new Tournament
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/tournaments")
    public Tournament createTournament(@Valid @RequestBody Tournament tournament) {
        Tournament savedTournament = tournamentService.createTournament(tournament);
        if (savedTournament ==  null) throw new TournamentExistsException(tournament.getId());
        return savedTournament;
    }

    // Get all Tournaments
    @GetMapping("/tournaments")
    public List<Tournament> getAllTournaments() {
        return tournamentService.getAllTournaments();
    }

    // Get a specific Tournament by ID
    @GetMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable Long id) {
        Optional<Tournament> tournament = tournamentService.getTournamentById(id);
        return tournament.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a Tournament by ID
    @PutMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> updateTournament(
            @PathVariable Long id, @RequestBody Tournament tournamentDetails) {
        try {
            Tournament updatedTournament = tournamentService.updateTournament(id, tournamentDetails);
            return ResponseEntity.ok(updatedTournament);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a Tournament by ID
    @DeleteMapping("/tournaments/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        try {
            tournamentService.deleteTournament(id);
            return ResponseEntity.noContent().build();
        } catch (TournamentNotFoundException e) { // RuntimeException
            return ResponseEntity.notFound().build();
        }
    }
}
