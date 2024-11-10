package csd.cuemaster.tournament;

import java.util.List;

import csd.cuemaster.match.Match;

public interface TournamentService {
    Tournament createTournament(Tournament tournament);
    List<Tournament> getAllTournaments();
    Tournament getTournamentById(Long id);
    Tournament updateTournament(Long id, Tournament tournamentDetails);
    void deleteTournament(Long id);

    Tournament joinTournament(Long tournamentId, Long playerId);
    Tournament leaveTournament(Long tournamentId, Long playerId);

    List<Match> createMatchesFromTournaments(Long tournamentId);
}
