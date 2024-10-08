package csd.cuemaster.tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TournamentServiceImpl implements TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    // Create a new tournament
    @Override
    public Tournament createTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    // Get all tournaments
    @Override
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    // Get a tournament by ID
    @Override
    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    // Update a tournament
    @Override
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
            
    }

    // Delete a tournament
    @Override
    public void deleteTournament(Long id) {
        tournamentRepository.deleteById(id);
    }
}
