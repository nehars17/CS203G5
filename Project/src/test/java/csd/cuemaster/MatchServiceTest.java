package csd.cuemaster;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import csd.cuemaster.profile.ProfileService;
import csd.cuemaster.services.MatchingService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootTest
public class MatchServiceTest {

    @InjectMocks
    private MatchServiceImpl matchService;

    @Mock
    private ProfileService profileService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteMatchById_Success() {
        Long matchId = 1L;
        when(matchRepository.existsById(matchId)).thenReturn(true);

        matchService.deleteMatchById(matchId);

        verify(matchRepository, times(1)).deleteById(matchId);
    }

    @Test
    void testDeleteMatchById_NotFound() {
        Long matchId = 1L;
        when(matchRepository.existsById(matchId)).thenReturn(false);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            matchService.deleteMatchById(matchId);
        });

        assertEquals("Match with ID 1 not found", exception.getMessage());
        verify(matchRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateMatch_Success() {
        Long matchId = 1L;
        Match existingMatch = new Match();
        existingMatch.setId(matchId);

        Match updatedMatch = new Match();
        updatedMatch.setMatchDate(LocalDate.now());
        updatedMatch.setMatchTime(LocalTime.now());

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(existingMatch));
        when(matchRepository.save(any(Match.class))).thenReturn(existingMatch);

        Match result = matchService.updateMatch(matchId, updatedMatch);

        assertNotNull(result);
        verify(matchRepository, times(1)).save(existingMatch);
    }

    @Test
    void testUpdateMatch_NotFound() {
        Long matchId = 1L;
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            matchService.updateMatch(matchId, new Match());
        });

        assertEquals("Match not found with id: 1", exception.getMessage());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testGetMatchById_Success() {
        Long matchId = 1L;
        Match match = new Match();
        match.setId(matchId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        Match result = matchService.getMatchById(matchId);

        assertNotNull(result);
        assertEquals(matchId, result.getId());
    }

    @Test
    void testGetAllMatches() {
        List<Match> matches = new ArrayList<>();
        matches.add(new Match());

        when(matchRepository.findAll()).thenReturn(matches);

        List<Match> result = matchService.getAllMatches();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCreateMatchesFromTournament_WithSufficientPlayers() {
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_16);

        List<Profile> players = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            Profile player = new Profile();
            player.setId((long) i);
            players.add(player);
        }
        tournament.setPlayers(players);

        List<Match> mockMatches = new ArrayList<>();
        mockMatches.add(new Match());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchingService.createPairs(players, tournament)).thenReturn(mockMatches);
        when(matchRepository.saveAll(mockMatches)).thenReturn(mockMatches);

        List<Match> result = matchService.createMatchesFromTournaments(tournamentId);

        assertNotNull(result);
        assertEquals(mockMatches.size(), result.size());
        verify(tournamentRepository, times(1)).save(tournament);
        assertEquals(Tournament.Status.QUARTER_FINALS, tournament.getStatus());
    }

    @Test
    void testCreateMatchesFromTournament_WithInsufficientPlayers() {
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_16);

        List<Profile> players = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            Profile player = new Profile();
            player.setId((long) i);
            players.add(player);
        }
        tournament.setPlayers(players);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            matchService.createMatchesFromTournaments(tournamentId);
        });

        assertEquals("Insufficient players for this round. Required: 32", exception.getMessage());
    }

    @Test
    void testDeclareWinner_Success() {
        Long matchId = 1L;
        Long winnerId = 1L;

        Match match = new Match();
        match.setId(matchId);
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        match.setTournament(tournament);

        User winner = new User();
        winner.setId(winnerId);
        Profile winnerProfile = new Profile();
        winner.setProfile(winnerProfile);

        User loser = new User();
        loser.setId(2L);
        Profile loserProfile = new Profile();
        loser.setProfile(loserProfile);

        match.setUser1(winner);
        match.setUser2(loser);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(matchRepository.save(match)).thenReturn(match);

        matchService.declareWinner(matchId, winnerId);

        assertEquals(winner, match.getWinner());
        verify(profileService, times(1)).updatePlayerStatistics(winnerProfile, loserProfile, 1L, match.getTournamentStatus());
    }

    @Test
    void testDeclareWinner_InvalidWinner() {
        Long matchId = 1L;
        Long winnerId = 3L;

        Match match = new Match();
        match.setId(matchId);

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        match.setUser1(user1);
        match.setUser2(user2);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            matchService.declareWinner(matchId, winnerId);
        });

        assertEquals("Winner must be one of the two participants of the match.", exception.getMessage());
    }

    @Test
    void testUpdateTournamentStatus() {
        Tournament tournament = new Tournament();
        tournament.setStatus(Tournament.Status.ROUND_OF_16);

        matchService.updateTournamentStatus(tournament);

        assertEquals(Tournament.Status.QUARTER_FINALS, tournament.getStatus());
        verify(tournamentRepository, times(1)).save(tournament);
    }
}
