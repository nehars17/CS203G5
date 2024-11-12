package csd.cuemaster.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.tournament.*;
import csd.cuemaster.user.*;
import csd.cuemaster.profile.*;

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

    // public MatchServiceImpl(MatchRepository matchRepository) {
    //     this.matchResponsitory = matchRepository;
    // }
    //create new match
    @Override
    public Match createMatch(Match match) {
        if (match.getTournament() == null || !tournamentRepository.existsById(match.getTournament().getId())) {
            throw new ResourceNotFoundException("Tournament with ID " + match.getTournament().getId() + " does not exist");
        }
        if (!userRepository.existsById(match.getUser1().getId())) {
            throw new ResourceNotFoundException("User with ID " + match.getUser1().getId() + " does not exist");
        }
        if (!userRepository.existsById(match.getUser2().getId())) {
            throw new ResourceNotFoundException("User with ID " + match.getUser2().getId() + " does not exist");
        }
        
        return matchRepository.save(match);
    }

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
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new ResourceNotFoundException("Match with ID " + matchId + " does not exist"));
    
            // Check if the winner is one of the two users in the match
        if (!match.getUser1().getId().equals(winnerId) && !match.getUser2().getId().equals(winnerId)) {
            throw new IllegalArgumentException("Winner must be one of the two participants of the match.");
        }
    
            // Set the winner and update the match
        match.setWinner(userRepository.findById(winnerId).orElseThrow(() -> new ResourceNotFoundException("User with ID " + winnerId + " does not exist")));
        
        return matchRepository.save(match);
    }

    // Create matches from a given tournament.
    @Override
    public List<Match> createMatchesFromTournaments(Long tournamentId) {
        List<Profile> players = new ArrayList<>(profileService.getProfilesFromTournaments(tournamentId));
        if (players.size() < 2) {
            return new ArrayList<>();
        }
        List<Match> matches = new ArrayList<>();
        Random random = new Random();
        int pointsRange = 100;

        // Matchmaking occurs here.
        while (players.size() >= 2) {
            int player1 = random.nextInt(players.size());
            int player2 = random.nextInt(players.size());
            player2 = validatePlayer(players, random, player1, player2);
            int difference = getDifference(players, player1, player2);

            // Create match only when it is balanced.
            if (difference <= pointsRange) {
                createMatch(players, matches, player1, player2);
                setMatchCount(players, player1, player2);
                removePlayers(players, player1, player2);

                // Reset the range back to 100.
                pointsRange = 100;
            } else {
                pointsRange += 100;
            }
        }
        matchRepository.saveAll(matches);
        return matches;
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
}