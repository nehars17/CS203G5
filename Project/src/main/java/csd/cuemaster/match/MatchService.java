package csd.cuemaster.match;

import java.util.List;

public interface MatchService {

    Match createMatch(Match match);
    Match updateMatch(Long id, Match match);
    Match getMatchById(Long matchId);
    Match declareWinner(Long matchId, Long winnerId);
    List<Match> getAllMatches();
    void deleteMatchById(Long matchId);
    List<Match> getMatchesByTournamentId(Long tournamentId);
    List<Match> createMatchesFromTournaments(Long tournamentId);
}