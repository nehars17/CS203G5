package csd.cuemaster.tournament;

import java.util.Optional;

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
    // Explicitly defined method to find a tournament by its ID
    @Override
    Optional<Tournament> findById(Long id);
    
    // Method to find a tournament by its name
    Optional<Tournament> findByTournamentname(String tournamentname);
}
