package csd.cuemaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.match.MatchServiceImpl;
import csd.cuemaster.match.ResourceNotFoundException;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootTest
class MatchServiceTest {

    @InjectMocks
    private MatchServiceImpl matchService;  

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

@Test
void testCreateMatch_Success() {
    // Setup
    Match match = new Match();
    Tournament tournament = new Tournament();
    tournament.setId(1L);
    match.setTournament(tournament);
    
    User user1 = new User();
    user1.setId(1L);
    match.setUser1(user1);

    User user2 = new User();
    user2.setId(2L);
    match.setUser2(user2);

    when(tournamentRepository.existsById(1L)).thenReturn(true);
    when(userRepository.existsById(1L)).thenReturn(true);
    when(userRepository.existsById(2L)).thenReturn(true);
    when(matchRepository.save(any(Match.class))).thenReturn(match);

    // Execute
    Match createdMatch = matchService.createMatch(match);

    // Verify
    assertNotNull(createdMatch);
    verify(matchRepository, times(1)).save(any(Match.class));
}

    @Test
    void testCreateMatch_TournamentNotFound() {
        // Setup
        Match match = new Match();
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        match.setTournament(tournament);

        when(tournamentRepository.existsById(1L)).thenReturn(false);

        // Execute & Verify
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            matchService.createMatch(match);
        });

        assertEquals("Tournament with ID 1 does not exist", exception.getMessage());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testUpdateMatch_Success() {
        // Setup
        Long matchId = 1L;
        Match existingMatch = new Match();
        existingMatch.setId(matchId);

        Match updatedMatch = new Match();
        updatedMatch.setTournament(new Tournament());
        updatedMatch.setUser1(new User());
        updatedMatch.setUser2(new User());
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(existingMatch));
        when(matchRepository.save(existingMatch)).thenReturn(existingMatch);

        // Execute
        Match result = matchService.updateMatch(matchId, updatedMatch);

        // Verify
        assertNotNull(result);
        verify(matchRepository, times(1)).save(existingMatch);
    }

    @Test
    void testUpdateMatch_NotFound() {
        // Setup
        Long matchId = 1L;
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            matchService.updateMatch(matchId, new Match());
        });

        assertEquals("Match not found with id: 1", exception.getMessage());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testGetMatchById_Success() {
        // Setup
        Long matchId = 1L;
        Match match = new Match();
        match.setId(matchId);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        // Execute
        Match result = matchService.getMatchById(matchId);

        // Verify
        assertNotNull(result);
        assertEquals(matchId, result.getId());
    }

    @Test
    void testGetMatchById_NotFound() {
        // Setup
        Long matchId = 1L;
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        // Execute
        Match result = matchService.getMatchById(matchId);

        // Verify
        assertNull(result);
    }

    @Test
    void testGetAllMatches() {
        // Setup
        List<Match> matches = new ArrayList<>();
        matches.add(new Match());
        
        when(matchRepository.findAll()).thenReturn(matches);

        // Execute
        List<Match> result = matchService.getAllMatches();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteMatchById_Success() {
        // Setup
        Long matchId = 1L;
        when(matchRepository.existsById(matchId)).thenReturn(true);

        // Execute
        matchService.deleteMatchById(matchId);

        // Verify
        verify(matchRepository, times(1)).deleteById(matchId);
    }

    @Test
    void testDeleteMatchById_NotFound() {
        // Setup
        Long matchId = 1L;
        when(matchRepository.existsById(matchId)).thenReturn(false);

        // Execute & Verify
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            matchService.deleteMatchById(matchId);
        });

        assertEquals("This match with id:1 does not exist", exception.getMessage());
        verify(matchRepository, never()).deleteById(matchId);
    }

    @Test
    void testGetMatchesByTournamentId() {
        // Setup
        Long tournamentId = 1L;
        List<Match> matches = new ArrayList<>();
        matches.add(new Match());

        when(matchRepository.findByTournamentId(tournamentId)).thenReturn(matches);

        // Execute
        List<Match> result = matchService.getMatchesByTournamentId(tournamentId);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeclareWinner_Success() {
        // Setup
        Long matchId = 1L;
        Long winnerId = 1L;
        
        Match match = new Match();
        User user1 = new User();
        user1.setId(winnerId);
        User user2 = new User();
        user2.setId(2L);
        
        match.setUser1(user1);
        match.setUser2(user2);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(winnerId)).thenReturn(Optional.of(user1));
        when(matchRepository.save(match)).thenReturn(match);

        // Execute
        Match result = matchService.declareWinner(matchId, winnerId);

        // Verify
        assertNotNull(result);
        assertEquals(winnerId, result.getWinner().getId());
    }

    @Test
    void testDeclareWinner_InvalidWinner() {
        // Setup
        Long matchId = 1L;
        Long winnerId = 3L;
        
        Match match = new Match();
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        
        match.setUser1(user1);
        match.setUser2(user2);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        // Execute & Verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            matchService.declareWinner(matchId, winnerId);
        });

        assertEquals("Winner must be one of the two participants of the match.", exception.getMessage());
    }
}