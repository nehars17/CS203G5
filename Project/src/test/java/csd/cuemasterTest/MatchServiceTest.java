package csd.cuemasterTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.match.MatchService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;

public class MatchServiceTest {

    @InjectMocks
    private MatchService matchService;

    @Mock
    private MatchRepository matchRepository;

    private Match match;
    private Tournament tournament;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock data for testing
        tournament = new Tournament("Tournament1", LocalDate.of(2024, 10, 1), LocalTime.of(12, 0), "Location1", "Upcoming");
        
        user1 = new User("Player1", "password1", "ROLE_PLAYER");
        
        user2 = new User("Player2", "password2", "ROLE_PLAYER");
        
        match = new Match(tournament, user1, user2, LocalDate.of(2024, 10, 2), LocalTime.of(12, 0), 0, 0);
    }

    @Test
    void testSaveMatch() {
        // Set an ID for the mock match to simulate saving
        match.setId(1L); 
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        Match savedMatch = matchService.saveMatch(match);
        assertNotNull(savedMatch);
        assertEquals(match.getId(), savedMatch.getId()); // Check if the ID matches
    }

    @Test
    void testGetMatchById() {
        // Set ID for the match before returning it
        match.setId(1L);
        when(matchRepository.findById(anyLong())).thenReturn(Optional.of(match));

        Match foundMatch = matchService.getMatchById(1L);
        assertNotNull(foundMatch);
        assertEquals(match.getId(), foundMatch.getId());
    }

    @Test
    void testGetMatchById_NotFound() {
        when(matchRepository.findById(anyLong())).thenReturn(Optional.empty());

        Match foundMatch = matchService.getMatchById(1L);
        assertNull(foundMatch);
    }

    @Test
    void testDeleteMatchById() {
        doNothing().when(matchRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> matchService.deleteMatchById(1L));
        verify(matchRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateMatch() {
        match.setId(1L); // Set the ID for the existing match
        Match updatedMatch = new Match(match.getTournament(), match.getUser1(), match.getUser2(), LocalDate.of(2024, 10, 2), LocalTime.of(12, 0), 9, 8);
        updatedMatch.setId(1L); // Set ID for the updated match

        when(matchRepository.findById(anyLong())).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(updatedMatch);

        Match result = matchService.saveMatch(updatedMatch);
        assertNotNull(result);
        assertEquals(9, result.getUser1Score()); // Check if the user1Score matches
    }
}