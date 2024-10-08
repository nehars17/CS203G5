package csd.cuemaster.match;

import java.util.List;

public interface MatchService {

    Match createMatch(Match match);
    Match updateMatch(Long id, Match match);
    Match getMatchById(Long matchId);
    List<Match> getAllMatches();
    void deleteMatchById(Long matchId);
    List<Match> getMatchesByTournamentId(Long tournamentId);
}
