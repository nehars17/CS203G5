package csd.cuemaster.tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.match.MatchService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class TournamentServiceImpl implements TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;
    
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
        Tournament tournament = getTournamentById(tournamentId);
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!tournament.getPlayers().contains(player.getProfile())) {
            tournament.getPlayers().add(player.getProfile());
            tournamentRepository.save(tournament);
        }

        return tournament;
    }

    @Transactional
    @Override
    public Tournament leaveTournament(Long tournamentId, Long playerId) {
        Tournament tournament = getTournamentById(tournamentId);
        User player;
        player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tournament.getPlayers().remove(player);
        tournamentRepository.save(tournament);

        return tournament;
    }

    @Override
    public List<Match> createMatchesFromTournaments(Long tournamentId) {
        return matchService.createMatchesFromTournaments(tournamentId);
    }

    // /**
    //  * Ensure there are enough players for the current round.
    //  */
    // private void checkSufficientPlayers(List<Profile> players, int requiredPlayers) {
    //     if (players.size() < requiredPlayers) {
    //         throw new IllegalArgumentException("Insufficient players for this round");
    //     }
    // }
}