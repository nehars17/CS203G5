package csd.cuemaster.match;

import java.util.List;

public interface MatchService {

    List<Match> createMatchesFromTournaments(Long tournamentId);

    Match updateMatch(Long id, Match match);
    
    void deleteMatchById(Long matchId);

    void declareWinner(Long matchId, Long winnerId);
    
    List<Match> getAllMatches();
    Match getMatchById(Long matchId);
}