package csd.cuemaster;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileServiceImpl;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootTest
class MatchServiceTest {

    @InjectMocks
    private MatchServiceImpl matchService;

    @Mock
    private ProfileServiceImpl profileService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

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

    // Test Case: Get list of matches from a tournament.
    @Test
    void createMatchesFromTournaments_TwoPlayerProfiles_ReturnList() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user1);
        profile1.setId(1L);
        user1.setProfile(profile1);
        profile1.setPoints(1200);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", user2);
        profile2.setId(2L);
        user2.setProfile(profile2);
        profile2.setPoints(2300);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.getPlayers().add(1L);
        tournament.getPlayers().add(2L);

        // Mock
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(profileService.sortProfilesFromTournaments(1L)).thenReturn(List.of(profile1, profile2));

        // Act
        List<Match> retrievedMatches = matchService.createMatchesFromTournaments(1L);

        // Assert
        assertNotNull(retrievedMatches);
        assertFalse(retrievedMatches.isEmpty());
        assertEquals(1, retrievedMatches.size());
    }

    // Test Case: Not enough players to create matches.
    @Test
    void createMatchesFromTournaments_OnePlayerProfile_ReturnEmptyList() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user1);
        profile1.setId(1L);
        user1.setProfile(profile1);
        profile1.setPoints(1200);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.getPlayers().add(1L);

        // Mock
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(profileService.sortProfilesFromTournaments(1L)).thenReturn(List.of(profile1));

        // Act
        List<Match> retrievedMatches = matchService.createMatchesFromTournaments(1L);

        // Assert
        assertNotNull(retrievedMatches);
        assertTrue(retrievedMatches.isEmpty());
    }
}