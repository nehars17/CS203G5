package csd.cuemaster.match;

import java.util.List; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MatchRepository extends JpaRepository <Match, Long> {

    // Custom query to find all matches by tournament ID
    List<Match> findByTournamentId(Long tournamentId);

    // Custom query to find matches by user ID (player)
    List<Match> findByUser1IdOrUser2Id(Long userId1, Long userId2);
}