package csd.cuemaster.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileService;
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
    private ProfileRepository profileRepository;

    @Override
    public List<Match> createMatchesFromTournaments(Long tournamentId) {

        
        // Retrieve the tournament from the repository using the tournamentId
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
    
        List<Profile> players;
    
        // If the tournament is in its first round, fetch initial players.
        if (tournament.getStatus() == Tournament.Status.ROUND_OF_32) {
            players = new ArrayList<>(profileService.getProfilesFromTournaments(tournamentId));
        } else {
            // Otherwise, get winners from the previous round
            players = getWinnersFromPreviousRound(tournament);
        }
    
        // Ensure sufficient players to proceed in the tournament bracket.
        checkSufficientPlayers(players, getRequiredPlayersForNextRound(tournament));
    
        if (players.size() < 2) {
            return new ArrayList<>();
        }
    
        List<Match> matches = new ArrayList<>();
        Random random = new Random();
        int pointsRange = 100;
    
        // Matchmaking for the current round.
        while (players.size() > 2) {
            int player1 = random.nextInt(players.size());
            int player2 = random.nextInt(players.size());
            player2 = validatePlayer(players, random, player1, player2);
            int difference = getDifference(players, player1, player2);
    
            // Only create a match if it's balanced within the specified points range.
            if (difference <= pointsRange) {
                createMatch(players, matches, player1, player2);
                removePlayers(players, player1, player2);
    
                // Reset the range back to 100 after a successful pairing.
                pointsRange = 100;
            } else {
                pointsRange += 100;
            }
        }
    
        // If exactly two players remain, create the final match for the round.
        if (players.size() == 2) {
            createLastMatch(players, matches);
        }
    
        // Save the newly created matches to the repository.
        matchRepository.saveAll(matches);
    
        // Update the tournament status to progress to the next round.
        updateTournamentStatus(tournament);
    
        return matches;
    }
    
    // //back up method for manual match creation
    // @Override
    // public Match createMatch(Match match) {
    //     if (match.getTournament() == null || !tournamentRepository.existsById(match.getTournament().getId())) {
    //         throw new ResourceNotFoundException("Tournament with ID " + match.getTournament().getId() + " does not exist");
    //     }
    //     if (!userRepository.existsById(match.getUser1().getId())) {
    //         throw new ResourceNotFoundException("User with ID " + match.getUser1().getId() + " does not exist");
    //     }
    //     if (!userRepository.existsById(match.getUser2().getId())) {
    //         throw new ResourceNotFoundException("User with ID " + match.getUser2().getId() + " does not exist");
    //     }
        
    //     return matchRepository.save(match);
    // }

    //update exisiting match
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
        // if (!matchRepository.existsById(matchId)) {
        //     return null;
        // }
        // match.setId(matchId);
        // return matchRepository.save(match);
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
    public void deleteMatchById(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("This match with id:" + matchId +" does not exist");
        }
        matchRepository.deleteById(matchId);
    }

    @Override
    public List<Match> getMatchesByTournamentId(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    // Set the winner of the match
    @Override
    public Match declareWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        User winner = userRepository.findById(winnerId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Profile winnerProfile = winner.getProfile();
        // Profile loserProfile = match.getUser1().getId().equals(winnerId)
        //     ? match.getUser2().getProfile()
        //     : match.getUser1().getProfile();

        match.setWinner(winner);
        matchRepository.save(match);

        Long tournamentId = match.getTournament().getId();

        // Update player statistics
        profileService.updatePlayerStatistics(matchId, winnerId);

        return match;
    }

    /* Helpers Methods */

    /** Prevent organizers from creating matches without enough players for the given starting round. Ex: Starting with Round of 32 will need 32 players  
     * @param players
     * @param requiredPlayers
     */
    private void checkSufficientPlayers(List<Profile> players, int requiredPlayers) {
        if (players.size() < requiredPlayers) {
            throw new IllegalArgumentException("Insufficient players for this round. Required: " + requiredPlayers);
        }
    }

    private int getRequiredPlayersForNextRound(Tournament tournament) {
        switch (tournament.getStatus()) {
            case ROUND_OF_32 -> {
                return 32;
            }
            case ROUND_OF_16 -> {
                return 16;
            }
            case QUARTER_FINALS -> {
                return 8;
            }
            case SEMI_FINAL -> {
                return 4;
            }
            case FINAL -> {
                return 2;
            }
            default -> throw new IllegalStateException("Invalid tournament status");
        }
    }

    // START OF HELPER METHODS

    // Helper method to check that the two players are different.
    private int validatePlayer(List<Profile> players, Random random, int player1, int player2) {
        while (player1 == player2) {
            player2 = random.nextInt(players.size());
        }
        return player2;
    }

    // Helper method to get the points difference between the two players.
    private int getDifference(List<Profile> players, int player1, int player2) {
        Integer points1 = players.get(player1).getPoints();
        Integer points2 = players.get(player2).getPoints();
        int difference = Math.abs(points1 - points2);
        return difference;
    }

    // Helper method to create matches.
    private void createMatch(List<Profile> players, List<Match> matches, int player1, int player2) {
        User user1 = players.get(player1).getUser();
        User user2 = players.get(player2).getUser();
        Match match = new Match();
        matches.add(match);
        match.setUser1(user1);
        match.setUser2(user2);
    }

    // Helper method to increase match count of the chosen players.
    private void setMatchCount(List<Profile> players, int player1, int player2) {
        Integer matchCount1 = players.get(player1).getMatchCount();
        Integer matchCount2 = players.get(player2).getMatchCount();
        players.get(player1).setMatchCount(matchCount1 + 1);
        players.get(player2).setMatchCount(matchCount2 + 1);
        profileRepository.saveAll(players);
    }

    // Helper method to remove the chosen players from the list.
    private void removePlayers(List<Profile> players, int player1, int player2) {
        if (player1 > player2) {
            players.remove(player1);
            players.remove(player2);
        } else if (player2 > player1) {
            players.remove(player2);
            players.remove(player1);
        }
    }

    // Helper method to create the last match if there are exactly two players left.
    private void createLastMatch(List<Profile> players, List<Match> matches) {
        User user1 = players.get(0).getUser();
        User user2 = players.get(1).getUser();
        Match match = new Match();
        matches.add(match);
        match.setUser1(user1);
        match.setUser2(user2);
        players.clear();
    }

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

    public List<Profile> getWinnersFromPreviousRound(Tournament tournament) {
        Tournament.Status previousRoundStatus = getPreviousRoundStatus(tournament.getStatus());
        
        // Retrieve all matches from the previous round based on tournament ID and previous round status
        List<Match> previousRoundMatches = matchRepository.findByTournamentIdAndStatus(tournament.getId(), previousRoundStatus);
        
        // Collect winners from these matches
        List<Profile> winners = new ArrayList<>();
        for (Match match : previousRoundMatches) {
            if (match.getWinner() != null) {
                winners.add(match.getWinner().getProfile());
            } else {
                throw new IllegalStateException("Match without a declared winner in round: " + previousRoundStatus);
            }
        }
    
        if (winners.size() < getRequiredPlayersForNextRound(tournament)) {
            throw new IllegalStateException("Not enough winners to create pairs for the next round");
        }
    
        return winners;
    }
    
    private Tournament.Status getPreviousRoundStatus(Tournament.Status currentStatus) {
        return switch (currentStatus) {
            case ROUND_OF_16 -> Tournament.Status.ROUND_OF_32;
            case QUARTER_FINALS -> Tournament.Status.ROUND_OF_16;
            case SEMI_FINAL -> Tournament.Status.QUARTER_FINALS;
            case FINAL -> Tournament.Status.SEMI_FINAL;
            default -> throw new IllegalStateException("No previous round for this status: " + currentStatus);
        };
    }
}