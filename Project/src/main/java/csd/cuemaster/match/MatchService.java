package csd.cuemaster.match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchService {
    
    @Autowired
    private MatchRepository matchRepository;

    //create or update match
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId).orElse(null);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public void deleteMatchById(Long matchId) {
        matchRepository.deleteById(matchId);
    }

    public List<Match> getMatchesByTournamentId(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }
}
