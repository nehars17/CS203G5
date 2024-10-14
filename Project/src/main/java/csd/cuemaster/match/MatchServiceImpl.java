package csd.cuemaster.match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import csd.cuemaster.tournament.TournamentRepository;
// import csd.cuemaster.user.UserRepository;
import csd.cuemaster.tournament.*;
import csd.cuemaster.user.*;
import csd.cuemaster.match.*;

@Service
public class MatchServiceImpl implements MatchService {
    
    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentRepository tournamentRepository; 

    @Autowired
    private UserRepository userRepository; 

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
}
