// package csd.cuemaster;

// import java.time.LocalDate;
// import java.time.LocalTime;
// import java.util.ArrayList;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.when;
// import org.mockito.MockitoAnnotations;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// import csd.cuemaster.match.Match;
// import csd.cuemaster.match.MatchController;
// import csd.cuemaster.match.MatchService;
// import csd.cuemaster.tournament.Tournament;
// import csd.cuemaster.user.User;

// public class MatchIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Mock
//     private MatchService matchService;

//     @InjectMocks
//     private MatchController matchController;

//     private Match match;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         mockMvc = MockMvcBuilders.standaloneSetup(matchController).build(); // Set up MockMvc

//         // Create mock User objects
//         User user1 = new User();
//         user1.setId(100L);

//         User user2 = new User();
//         user2.setId(200L);

//         Tournament tournament = new Tournament(); 

//         // Initialize Match object
//         match = new Match(tournament, user1, user2, LocalDate.now(), LocalTime.now(), 0, 0);
//         match.setId(1L);
//     }

//     @Test
//     void testCreateMatch() throws Exception {
//         when(matchService.createMatch(any(Match.class))).thenReturn(match); // Mock service response

//         mockMvc.perform(post("/matches/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"tournament\": {}, \"user1\": {}, \"user2\": {}, \"matchDate\": \"" + LocalDate.now() + "\", \"matchTime\": \"" + LocalTime.now() + "\", \"user1Score\": 0, \"user2Score\": 0}"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("match created: id =1")); // Adjust expected output
//     }

//     @Test
//     void testUpdateMatch() throws Exception {
//         when(matchService.updateMatch(eq(1L), any(Match.class))).thenReturn(match); // Mock service response

//         mockMvc.perform(put("/matches/1")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"tournament\": {}, \"user1\": {}, \"user2\": {}, \"matchDate\": \"" + LocalDate.now() + "\", \"matchTime\": \"" + LocalTime.now() + "\", \"user1Score\": 1, \"user2Score\": 2}"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("match updated: id =1")); // Adjust expected output
//     }

//     @Test
//     void testGetMatchById() throws Exception {
//         when(matchService.getMatchById(1L)).thenReturn(match); // Mock service response

//         mockMvc.perform(get("/matches/1"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(1L)); // Adjust expected output
//     }

//     @Test
//     void testGetAllMatches() throws Exception {
//         List<Match> matches = new ArrayList<>();
//         matches.add(match);
//         when(matchService.getAllMatches()).thenReturn(matches); // Mock service response

//         mockMvc.perform(get("/matches"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$[0].id").value(1L)); // Adjust expected output
//     }

//     @Test
//     void testDeleteMatch() throws Exception {
//         doNothing().when(matchService).deleteMatchById(1L); // Mock service response

//         mockMvc.perform(delete("/matches/1"))
//                 .andExpect(status().isNoContent()); // Expecting HTTP 204 No Content
//     }

//     @Test
//     void testDeclareWinner() throws Exception {
//         when(matchService.declareWinner(1L, 100L)).thenReturn(match); // Mock service response

//         mockMvc.perform(post("/matches/1/declareWinner/100"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(1L)); // Adjust expected output
//     }
// }
