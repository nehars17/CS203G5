package csd.cuemasterTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchController;
import csd.cuemaster.match.MatchService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


public class MatchControllerTest {
    
    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private Match match;
    private Tournament tournament;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(matchController).build();

        // Mock data for testing
        tournament = new Tournament("Tournament1", LocalDate.of(2024, 10, 1), LocalTime.of(12, 0), "Location1", "Upcoming");
        
        user1 = new User("Player1", "password1", "ROLE_PLAYER");
        
        user2 = new User("Player2", "password2", "ROLE_PLAYER");
        
        match = new Match(tournament, user1, user2, LocalDate.of(2024, 10, 2), LocalTime.of(12, 0), 0, 0);
        //match.setId(1L);
    }


    @Test
    void testCreateMatch() throws Exception {
        when(matchService.saveMatch(any(Match.class))).thenReturn(match);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tournament\":{\"id\":1}, \"user1\":{\"id\":1}, \"user2\":{\"id\":2}, \"matchDate\":\"2024-10-02\", \"matchTime\":\"12:00\", \"user1Score\":8, \"user2Score\":7}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetMatchById() throws Exception {
        when(matchService.getMatchById(anyLong())).thenReturn(match);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/matches/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateMatch() throws Exception {
        Match updatedMatch = new Match(match.getTournament(), match.getUser1(), match.getUser2(), LocalDate.of(2024, 10, 02), LocalTime.of(12, 0), 9, 8);
        updatedMatch.setId(1L);

        when(matchService.getMatchById(anyLong())).thenReturn(match);
        when(matchService.saveMatch(any(Match.class))).thenReturn(updatedMatch);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/matches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tournament\":{\"id\":1}, \"user1\":{\"id\":1}, \"user2\":{\"id\":2}, \"matchDate\":\"2024-10-02\", \"matchTime\":\"12:00\", \"user1Score\":9, \"user2Score\":8}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user1Score").value(9));
    }

    @Test
    void testDeleteMatchById() throws Exception {
        doNothing().when(matchService).deleteMatchById(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/matches/1"))
            .andExpect(status().isNoContent());
    }
 
}
