package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentNotFoundException;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.tournament.TournamentServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    private Tournament tournament;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setTournamentname("New York Tournament");
        tournament.setLocation("New York");
        tournament.setStartDate(LocalDate.parse("2024-09-20")); // Set start date
        tournament.setEndDate(LocalDate.parse("2024-09-21"));   // Set end date
        tournament.setTime(LocalTime.parse("10:00:00"));        // Set time
        tournament.setStatus(Tournament.Status.UPCOMING);       // Set status using the enum
        tournament.setDescription("Test Tournament");
        tournament.setWinnerId(null);
        tournament.setPlayers(Arrays.asList(3L, 4L));
    }

    @Test
    void testCreateTournament() {
        // Arrange
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act
        Tournament createdTournament = tournamentService.createTournament(tournament);

        // Assert
        assertNotNull(createdTournament);
        assertEquals("New York Tournament", createdTournament.getTournamentname());
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void testGetAllTournaments() {
        // Arrange
        when(tournamentRepository.findAll()).thenReturn(Arrays.asList(tournament));

        // Act
        List<Tournament> tournaments = tournamentService.getAllTournaments();

        // Assert
        assertNotNull(tournaments);
        assertEquals(1, tournaments.size());
        verify(tournamentRepository, times(1)).findAll();
    }

    @Test
    void testGetTournamentById() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        // Act
        Tournament foundTournament = tournamentService.getTournamentById(1L);

        // Assert
        assertNotNull(foundTournament);
        assertEquals("New York Tournament", foundTournament.getTournamentname());
        verify(tournamentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTournamentById_NotFound() {
        // Arrange
        when(tournamentRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Tournament result = tournamentService.getTournamentById(2L);

        // Assert
        assertNull(result);
        verify(tournamentRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateTournament() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        Tournament updatedTournament = new Tournament();
        updatedTournament.setLocation("Los Angeles");
        updatedTournament.setStartDate(LocalDate.parse("2024-09-22")); // Set start date
        updatedTournament.setEndDate(LocalDate.parse("2024-09-23"));   // Set end date
        updatedTournament.setTime(LocalTime.parse("12:00:00"));        // Set time
        updatedTournament.setStatus(Tournament.Status.ONGOING);        // Set status using the enum
        updatedTournament.setDescription("Updated Tournament");
        updatedTournament.setWinnerId(3L);
        updatedTournament.setPlayers(Arrays.asList(3L, 4L));

        // Act
        Tournament result = tournamentService.updateTournament(1L, updatedTournament);

        // Assert
        assertNotNull(result);
        assertEquals("Los Angeles", result.getLocation());
        verify(tournamentRepository, times(1)).findById(1L);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testUpdateTournament_NotFound() {
        // Arrange
        when(tournamentRepository.findById(2L)).thenReturn(Optional.empty());

        Tournament updatedTournament = new Tournament();
        updatedTournament.setLocation("Los Angeles");

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> tournamentService.updateTournament(2L, updatedTournament));
        verify(tournamentRepository, times(1)).findById(2L);
    }

    @Test
    void testDeleteTournament() {
        // Arrange
        when(tournamentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(tournamentRepository).deleteById(1L);

        // Act
        tournamentService.deleteTournament(1L);

        // Assert
        verify(tournamentRepository, times(1)).existsById(1L);
        verify(tournamentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTournament_NotFound() {
        // Arrange
        when(tournamentRepository.existsById(2L)).thenReturn(false);

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> tournamentService.deleteTournament(2L));
        verify(tournamentRepository, times(1)).existsById(2L);
        verify(tournamentRepository, never()).deleteById(anyLong());
    }
}
