package csd.cuemaster.tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.match.ResourceNotFoundException;
import jakarta.transaction.Transactional;

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
    @Transactional
    @Override
    public Tournament updateTournament(Long id, Tournament tournamentDetails) {
        return tournamentRepository.findById(id).map(tournament -> {
            tournament.setTournamentname(tournamentDetails.getTournamentname());
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
        if (!tournamentRepository.existsById(id)) {
            throw new TournamentNotFoundException(id); // check if tournament id exists before attempting to delete it. If it doesnt exist, throw excpetion
        }
        tournamentRepository.deleteById(id);
    }

    @Transactional
    public Tournament joinTournament(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        
        // Add playerId to the list of players
        if (!tournament.getPlayers().contains(playerId)) {
            tournament.getPlayers().add(playerId);
            tournamentRepository.save(tournament);
        }
        
        return tournament;
    }

    @Transactional
    public Tournament leaveTournament(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        
        // Remove playerId from the list of players
        tournament.getPlayers().remove(playerId); // This will remove the player if they exist
        tournamentRepository.save(tournament);
        
        return tournament;
    }

}