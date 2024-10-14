package csd.cuemaster.tournament;

import java.util.List;

public interface TournamentService {
    Tournament createTournament(Tournament tournament);
    List<Tournament> getAllTournaments();
    Tournament getTournamentById(Long id);
    Tournament updateTournament(Long id, Tournament tournamentDetails);
    void deleteTournament(Long id);

}
