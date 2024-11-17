package csd.cuemaster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentController;
import csd.cuemaster.tournament.TournamentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class TournamentIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    private Tournament tournament;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController).build();

        // Initialize a sample tournament
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setTournamentname("New York Tournament");
        tournament.setLocation("New York");
        tournament.setStartDate(LocalDate.parse("2024-09-20"));
        tournament.setEndDate(LocalDate.parse("2024-09-21"));
        tournament.setTime(LocalTime.parse("10:00:00"));
        tournament.setStatus(Tournament.Status.UPCOMING);
        tournament.setDescription("Test Tournament");
        tournament.setWinnerId(null);
        tournament.setPlayers(List.of(3L, 4L));
    }

    @Test
    void testCreateTournament() throws Exception {
        when(tournamentService.createTournament(any(Tournament.class))).thenReturn(tournament);

        mockMvc.perform(post("/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tournamentname\": \"New York Tournament\", \"location\": \"New York\", " +
                        "\"startDate\": \"2024-09-20\", \"endDate\": \"2024-09-21\", \"time\": \"10:00:00\", " +
                        "\"status\": \"UPCOMING\", \"description\": \"Test Tournament\", \"players\": [3, 4]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tournamentname").value("New York Tournament"));
    }

    @Test
    void testGetAllTournaments() throws Exception {
        when(tournamentService.getAllTournaments()).thenReturn(Collections.singletonList(tournament));

        mockMvc.perform(get("/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tournamentname").value("New York Tournament"));
    }

    @Test
    void testGetTournamentById_ValidId() throws Exception {
        when(tournamentService.getTournamentById(1L)).thenReturn(tournament);

        mockMvc.perform(get("/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tournamentname").value("New York Tournament"));
    }

    @Test
    void testUpdateTournament() throws Exception {
        when(tournamentService.updateTournament(eq(1L), any(Tournament.class))).thenReturn(tournament);

        mockMvc.perform(put("/tournaments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tournamentname\": \"Updated Tournament\", \"location\": \"Updated Location\", " +
                        "\"startDate\": \"2024-09-22\", \"endDate\": \"2024-09-23\", \"time\": \"12:00:00\", " +
                        "\"status\": \"ONGOING\", \"description\": \"Updated Description\", \"players\": [3, 4]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tournamentname").value("New York Tournament"));
    }

    @Test
    void testDeleteTournament() throws Exception {
        doNothing().when(tournamentService).deleteTournament(1L);

        mockMvc.perform(delete("/tournaments/1"))
                .andExpect(status().isOk());
    }
}
