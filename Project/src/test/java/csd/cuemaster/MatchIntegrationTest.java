package csd.cuemaster;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MatchIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testCreateMatchesForNextRound() throws Exception {
        Long tournamentId = 1L;
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Match match = new Match();
            match.setId((long) i);
            matches.add(match);
        }

        when(matchService.createMatchesFromTournaments(tournamentId)).thenReturn(matches);

        mockMvc.perform(post(baseUrl + port + "/matches/tournament/{tournamentId}", tournamentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(16)));
    }

    @Test
    void testGetAllMatches() throws Exception {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Match match = new Match();
            match.setId((long) i);
            matches.add(match);
        }

        when(matchService.getAllMatches()).thenReturn(matches);

        mockMvc.perform(get(baseUrl + port + "/matches")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    void testGetMatchesByTournamentId() throws Exception {
        Long tournamentId = 1L;
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Match match = new Match();
            match.setId((long) i);
            matches.add(match);
        }

        when(matchService.getMatchesByTournamentId(tournamentId)).thenReturn(matches);

        mockMvc.perform(get(baseUrl + port + "/matches/tournament/{tournamentId}", tournamentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)));
    }
}