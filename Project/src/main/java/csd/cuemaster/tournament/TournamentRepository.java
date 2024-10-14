package csd.cuemaster.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * We only need this interface declaration
 * Spring will automatically generate an implementation of the repo
 * 
 * 
 */

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    
}
