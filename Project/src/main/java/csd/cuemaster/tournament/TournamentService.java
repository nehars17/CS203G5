package csd.cuemaster.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    // Create a new tournament
    public Tournament createTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    // Get all tournaments
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    // Get a tournament by ID
    public Optional<Tournament> getTournamentById(Long id) {
        return tournamentRepository.findById(id);
    }

    // Update a tournament
    public Tournament updateTournament(Long id, Tournament tournamentDetails) {
        return tournamentRepository.findById(id).map(tournament -> {
            tournament.setLocation(tournamentDetails.getLocation());
            tournament.setStartDate(tournamentDetails.getStartDate());
            tournament.setEndDate(tournamentDetails.getEndDate());
            tournament.setTime(tournamentDetails.getTime());
            tournament.setStatus(tournamentDetails.getStatus());
            tournament.setDescription(tournamentDetails.getDescription());
            tournament.setWinnerId(tournamentDetails.getWinnerId());
            tournament.setPlayers(tournamentDetails.getPlayers());
            return tournamentRepository.save(tournament);
        }).orElseThrow(() -> new TournamentNotFoundException(id));
            
        // }).orElseThrow(() -> new RuntimeException("Tournament not found with id " + id));
    }

    // Delete a tournament
    public void deleteTournament(Long id) {
        tournamentRepository.deleteById(id);
    }
}
