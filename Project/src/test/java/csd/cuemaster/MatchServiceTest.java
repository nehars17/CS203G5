package csd.cuemaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.match.MatchServiceImpl;
import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

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
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateMatchesFromTournaments_FirstRound_SufficientPlayers() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_32);

        List<Profile> profiles = new ArrayList<>();
        for (Long i = (long) 1; i <= 32; i++) {
            Profile profile = new Profile();
            profile.setId(i);
            profile.setPoints((int) (100 + i));

            User user = new User();
            user.setId(i);
            profile.setUser(user);

            profiles.add(profile);
        }

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(profileService.getProfilesFromTournaments(tournamentId)).thenReturn(profiles);
        when(matchRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);

        // Assert
        assertEquals(16, matches.size());
        verify(matchRepository, times(1)).saveAll(matches);
    }

    @Test
    void testCreateMatchesFromTournaments_FirstRound_InsufficientPlayers() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_32);

        List<Profile> profiles = new ArrayList<>();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(profileService.getProfilesFromTournaments(tournamentId)).thenReturn(profiles);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> matchService.createMatchesFromTournaments(tournamentId));

        assertEquals("Insufficient players for this round. Required: 32", exception.getMessage());
        verify(matchRepository, never()).saveAll(anyList());
    }
  
    @Test
    void testCreateMatchesFromTournaments_SubsequentRound_InsufficientWinners() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_16);
    
        // Winners from the previous round (ROUND_OF_32)
        List<Match> previousRoundMatches = new ArrayList<>();
        for (Long i = 1L; i <= 8; i++) { // Insufficient winners (only 8 instead of 16)
            Profile winnerProfile = new Profile();
            winnerProfile.setId(i);
            winnerProfile.setPoints((int) (200 + i));
    
            User winnerUser = new User();
            winnerUser.setId(i);
            winnerProfile.setUser(winnerUser);
    
            Match match = new Match();
            match.setWinner(winnerUser); // Set the winner
            previousRoundMatches.add(match);
        }
    
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findByTournamentIdAndStatus(tournamentId, Tournament.Status.ROUND_OF_32))
                .thenReturn(previousRoundMatches);
    
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> matchService.createMatchesFromTournaments(tournamentId));
    
        assertEquals("Not enough winners to create pairs for the next round", exception.getMessage());
        verify(matchRepository, never()).saveAll(anyList());
    }
    

    @Test
    void testCreateMatchesFromTournaments_BalancedMatchCreation() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.ROUND_OF_32);

        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            Profile profile = new Profile();
            profile.setId((long) i);
            profile.setPoints(100 + i);

            User user = new User();
            user.setId((long) i);
            profile.setUser(user);

            profiles.add(profile);
        }

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(profileService.getProfilesFromTournaments(tournamentId)).thenReturn(profiles);
        when(matchRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);

        // Assert
        assertEquals(16, matches.size());
        verify(matchRepository, times(1)).saveAll(matches);
    }

    @Test
    void testCreateMatchesFromTournaments_QuarterFinals_SufficientWinners() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.QUARTER_FINALS);
    
        // Mock winners from ROUND_OF_16
        List<Match> previousRoundMatches = new ArrayList<>();
        for (long i = 1; i <= 8; i++) {
            Profile winnerProfile = new Profile();
            winnerProfile.setId(i);
            winnerProfile.setPoints(200 + (int) i); // Example points
    
            User winnerUser = new User();
            winnerUser.setId(i);
            winnerUser.setProfile(winnerProfile);
    
            Match match = new Match();
            match.setWinner(winnerUser);
    
            previousRoundMatches.add(match);
        }
    
        // Mock repository responses
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findByTournamentIdAndStatus(tournamentId, Tournament.Status.ROUND_OF_16))
                .thenReturn(previousRoundMatches);
    
        // Act
        List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);
    
        // Assert
        assertEquals(4, matches.size()); // Expect 4 matches
        verify(matchRepository, times(1)).saveAll(matches);
    }
    
    @Test
    void testCreateMatchesFromTournaments_SemiFinals_SufficientWinners() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.SEMI_FINAL);
    
        // Mock winners from QUARTER_FINALS
        List<Match> previousRoundMatches = new ArrayList<>();
        for (long i = 1; i <= 4; i++) {
            Profile winnerProfile = new Profile();
            winnerProfile.setId(i);
            winnerProfile.setPoints(300 + (int) i); // Example points
    
            User winnerUser = new User();
            winnerUser.setId(i);
            winnerUser.setProfile(winnerProfile);
    
            Match match = new Match();
            match.setWinner(winnerUser);
    
            previousRoundMatches.add(match);
        }
    
        // Mock repository responses
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findByTournamentIdAndStatus(tournamentId, Tournament.Status.QUARTER_FINALS))
                .thenReturn(previousRoundMatches);
    
        // Act
        List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);
    
        // Assert
        assertEquals(2, matches.size()); // Expect 2 matches
        verify(matchRepository, times(1)).saveAll(matches);
    }
     
    @Test
    void testCreateMatchesFromTournaments_Final_SufficientWinners() {
        // Arrange
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setStatus(Tournament.Status.FINAL);
    
        // Mock winners from SEMI_FINAL
        List<Match> previousRoundMatches = new ArrayList<>();
        for (long i = 1; i <= 2; i++) {
            Profile winnerProfile = new Profile();
            winnerProfile.setId(i);
            winnerProfile.setPoints(400 + (int) i); // Example points
    
            User winnerUser = new User();
            winnerUser.setId(i);
            winnerUser.setProfile(winnerProfile);
    
            Match match = new Match();
            match.setWinner(winnerUser);
    
            previousRoundMatches.add(match);
        }
    
        // Mock repository responses
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findByTournamentIdAndStatus(tournamentId, Tournament.Status.SEMI_FINAL))
                .thenReturn(previousRoundMatches);
    
        // Act
        List<Match> matches = matchService.createMatchesFromTournaments(tournamentId);
    
        // Assert
        assertEquals(1, matches.size()); // Expect 1 match
        verify(matchRepository, times(1)).saveAll(matches);
    }

}
