package csd.cuemaster.match;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileService;
import csd.cuemaster.services.MatchingService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository; 

    @Autowired
    private MatchingService matchingService;  // Inject MatchingServiceImpl


    /**
     * Deletes a match by its ID.
     *
     * @param matchId ID of the match to delete.
     * @throws ResourceNotFoundException if the match with the given ID is not found.
     */
    @Override
    public void deleteMatchById(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match with ID " + matchId + " not found");
        }
        matchRepository.deleteById(matchId);
    }

        /**
     * only updates given a valid match id
     */
    @Override
    public Match updateMatch(Long matchId, Match match) {
        
        Match existingMatch = matchRepository.findById(matchId).orElse(null);
        
        if (existingMatch == null) {
            throw new RuntimeException("Match not found with id: " + matchId); // Consider a custom exception
        }

        // Update fields as necessary
        existingMatch.setTournament(match.getTournament());
        existingMatch.setUser1(match.getUser1());
        existingMatch.setUser2(match.getUser2());
        existingMatch.setMatchDate(match.getMatchDate());
        existingMatch.setMatchTime(match.getMatchTime());
        existingMatch.setUser1Score(match.getUser1Score());
        existingMatch.setUser2Score(match.getUser2Score());

        return matchRepository.save(existingMatch);
    }

    @Override
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId).orElse(null);
    }

    @Override
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    public List<Match> createMatchesFromTournaments(Long tournamentId) {
        // Retrieve the tournament from the repository using the tournamentId
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Ensure there are enough players for the round
        List<Profile> players = tournament.getPlayers();
        checkSufficientPlayers(players, getRequiredPlayersForNextRound(tournament));

        // Create match pairs based on tournament round status
        List<Match> matches = matchingService.createPairs(players, tournament);

        // Save the matches in the repository
        matchRepository.saveAll(matches);

        // Update the tournament status to the next round
        updateTournamentStatus(tournament);

        return matches;
    }

    private int getRequiredPlayersForNextRound(Tournament tournament) {
        switch (tournament.getStatus()) {
            case ROUND_OF_32 -> {
                return 64;
            }
            case ROUND_OF_16 -> {
                return 32;
            }
            case QUARTER_FINALS -> {
                return 16;
            }
            case SEMI_FINAL -> {
                return 8;
            }
            case FINAL -> {
                return 2;
            }
            default -> throw new IllegalStateException("Invalid tournament status");
        }
    }

    // private Match createByeMatch(Tournament tournament, User playerForBye) {
    //     return createMatch(tournament, playerForBye, null, "BYE");
    // }

    /**
     * Update the tournament status to the next round.
     */
    public void updateTournamentStatus(Tournament tournament) {
        switch (tournament.getStatus()) {
            case ROUND_OF_32 -> tournament.setStatus(Tournament.Status.ROUND_OF_16);
            case ROUND_OF_16 -> tournament.setStatus(Tournament.Status.QUARTER_FINALS);
            case QUARTER_FINALS -> tournament.setStatus(Tournament.Status.SEMI_FINAL);
            case SEMI_FINAL -> tournament.setStatus(Tournament.Status.FINAL);
            case FINAL -> tournament.setStatus(Tournament.Status.COMPLETED);
            default -> throw new IllegalStateException("Unexpected tournament status: " + tournament.getStatus());
        }
        tournamentRepository.save(tournament);
    }

    // // Helper method to create match pairs (even vs. odd player count, etc.)
    // private void createMatchPairs(List<User> players, List<Match> matches) {
    //     for (int i = 0; i < players.size(); i += 2) {
    //         if (i + 1 < players.size()) { // Only create pairs if there's a second player
    //             matches.add(createMatch(players.get(i), players.get(i + 1)));
    //         } else { // In case there's an odd number of players, handle "BYE" cases
    //             matches.add(createMatch(players.get(i), null));  // One player gets a bye
    //         }
    //     }
    // }

    private Match createMatch(User player1, User player2) {
        // Retrieve the tournament for the match by using either player1 or player2
        Tournament tournament = getTournamentForMatch(player1, player2);
        LocalDate matchDate = LocalDate.now();
        LocalTime matchTime = LocalTime.now();

        return new Match(tournament, player1, player2, matchDate, matchTime, 0, 0, "UPCOMING");
    }

    // Utility method to get the tournament for a given match based on the players
    private Tournament getTournamentForMatch(User player1, User player2) {
        // Find a match for the players
        Match existingMatch = matchRepository.findByUser1IdAndUser2Id(player1.getId(), player2.getId());
        
        if (existingMatch != null) {
            return existingMatch.getTournament();  // Return the tournament associated with the match
        } else {
            // If no existing match is found, you may choose to throw an error or create a new tournament
            throw new RuntimeException("No existing match found for players: " + player1.getUsername() + " and " + player2.getUsername());
        }
    }

    @Override
    public void declareWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        User winner = userRepository.findById(winnerId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Profile winnerProfile = winner.getProfile();
        Profile loserProfile = match.getUser1().getId().equals(winnerId)
            ? match.getUser2().getProfile()
            : match.getUser1().getProfile();

        match.setWinner(winner);
        matchRepository.save(match);

        Long tournamentId = match.getTournament().getId();

        // Update player statistics
        profileService.updatePlayerStatistics(winnerProfile, loserProfile, tournamentId, match.getTournamentStatus());
    }

    /** Prevent organizers from creating matches without enough players for the given starting round. Ex: Starting with Round of 32 will need 64 players  
     * @param players
     * @param requiredPlayers
     */
    private void checkSufficientPlayers(List<Profile> players, int requiredPlayers) {
        if (players.size() < requiredPlayers) {
            throw new IllegalArgumentException("Insufficient players for this round. Required: " + requiredPlayers);
        }
    }
}