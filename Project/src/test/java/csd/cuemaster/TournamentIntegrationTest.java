// package csd.cuemaster;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.net.URI;
// import java.time.LocalDate;
// import java.time.LocalTime;
// import java.util.Arrays;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.ResponseEntity;

// import csd.cuemaster.tournament.Tournament;
// import csd.cuemaster.tournament.TournamentRepository;

// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// class TournamentIntegrationTest {

//     @LocalServerPort
//     private int port;

//     private final String baseUrl = "http://localhost:";

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Autowired
//     private TournamentRepository tournamentRepository;

//     @AfterEach
//     void tearDown() {
//         tournamentRepository.deleteAll();
//     }

//     @Test
//     public void createTournament_Success() throws Exception {
//         URI uri = new URI(baseUrl + port + "/tournaments");
//         Tournament tournament = new Tournament();
//         tournament.setTournamentname("New York Tournament");
//         tournament.setLocation("New York");
//         tournament.setStartDate(LocalDate.parse("2024-09-20"));
//         tournament.setEndDate(LocalDate.parse("2024-09-21"));
//         tournament.setTime(LocalTime.parse("10:00:00"));
//         tournament.setStatus(Tournament.Status.UPCOMING);
//         tournament.setDescription("Test Tournament");
//         tournament.setWinnerId(null);
//         tournament.setPlayers(Arrays.asList(3L, 4L));

//         ResponseEntity<Tournament> result = restTemplate.postForEntity(uri, tournament, Tournament.class);

//         assertEquals(201, result.getStatusCode().value());
//         assertEquals("New York Tournament", result.getBody().getTournamentname());
//     }

//     @Test
//     public void getAllTournaments_Success() throws Exception {
//         Tournament tournament = new Tournament();
//         tournament.setTournamentname("New York Tournament");
//         tournament.setLocation("New York");
//         tournament.setStartDate(LocalDate.parse("2024-09-20"));
//         tournament.setEndDate(LocalDate.parse("2024-09-21"));
//         tournament.setTime(LocalTime.parse("10:00:00"));
//         tournament.setStatus(Tournament.Status.UPCOMING);
//         tournament.setDescription("Test Tournament");
//         tournament.setWinnerId(null);
//         tournament.setPlayers(Arrays.asList(3L, 4L));
//         tournamentRepository.save(tournament);

//         URI uri = new URI(baseUrl + port + "/tournaments");

//         ResponseEntity<Tournament[]> result = restTemplate.getForEntity(uri, Tournament[].class);

//         assertEquals(200, result.getStatusCode().value());
//         assertEquals(1, result.getBody().length);
//     }

//     @Test
//     public void getTournamentById_ValidId_Success() throws Exception {
//         Tournament tournament = new Tournament();
//         tournament.setTournamentname("New York Tournament");
//         tournament.setLocation("New York");
//         tournament.setStartDate(LocalDate.parse("2024-09-20"));
//         tournament.setEndDate(LocalDate.parse("2024-09-21"));
//         tournament.setTime(LocalTime.parse("10:00:00"));
//         tournament.setStatus(Tournament.Status.UPCOMING);
//         tournament.setDescription("Test Tournament");
//         tournament.setWinnerId(null);
//         tournament.setPlayers(Arrays.asList(3L, 4L));
//         Tournament savedTournament = tournamentRepository.save(tournament);

//         URI uri = new URI(baseUrl + port + "/tournaments/" + savedTournament.getId());

//         ResponseEntity<Tournament> result = restTemplate.getForEntity(uri, Tournament.class);

//         assertEquals(200, result.getStatusCode().value());
//         assertEquals("New York Tournament", result.getBody().getTournamentname());
//     }

//     @Test
//     public void getTournamentById_InvalidId_Failure() throws Exception {
//         // Prepare the URI for a non-existent tournament ID
//         URI uri = new URI(baseUrl + port + "/tournaments/999");

//         // Perform the GET request and expect a 404 Not Found status
//         ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

//         // Validate that the status code is 404
//         assertEquals(404, result.getStatusCode().value());
//     }


//     @Test
// 	public void updateTournament_ValidId_Success() throws Exception {
// 		// Create and save the initial tournament
// 		Tournament tournament = new Tournament();
// 		tournament.setTournamentname("New York Tournament");
// 		tournament.setLocation("New York");
// 		tournament.setStartDate(LocalDate.parse("2024-09-20"));
// 		tournament.setEndDate(LocalDate.parse("2024-09-21"));
// 		tournament.setTime(LocalTime.parse("10:00:00"));
// 		tournament.setStatus(Tournament.Status.UPCOMING);
// 		tournament.setDescription("Test Tournament");
// 		tournament.setWinnerId(null);
// 		tournament.setPlayers(Arrays.asList(3L, 4L));

// 		// Save the initial tournament
// 		Tournament savedTournament = tournamentRepository.save(tournament);

// 		// Prepare the URI for the update request
// 		URI uri = new URI(baseUrl + port + "/tournaments/" + savedTournament.getId());
		
// 		// Create the updated tournament details
// 		Tournament updatedTournament = new Tournament();
// 		updatedTournament.setTournamentname("Los Angeles Tournament"); // Update the name
// 		updatedTournament.setLocation("Los Angeles");
// 		updatedTournament.setStartDate(LocalDate.parse("2024-09-22"));
// 		updatedTournament.setEndDate(LocalDate.parse("2024-09-23"));
// 		updatedTournament.setTime(LocalTime.parse("12:00:00"));
// 		updatedTournament.setStatus(Tournament.Status.ONGOING);
// 		updatedTournament.setDescription("Updated Tournament");
// 		updatedTournament.setWinnerId(3L);
// 		updatedTournament.setPlayers(Arrays.asList(3L, 4L));

// 		// Perform the update
// 		ResponseEntity<Tournament> result = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(updatedTournament), Tournament.class);

// 		// Validate the response
// 		assertEquals(200, result.getStatusCode().value());
// 		assertEquals("Los Angeles Tournament", result.getBody().getTournamentname()); // Ensure the update is reflected

// 		// Retrieve the updated tournament to confirm the changes
// 		ResponseEntity<Tournament> getResult = restTemplate.getForEntity(uri, Tournament.class);
// 		assertEquals(200, getResult.getStatusCode().value());
// 		assertEquals("Los Angeles Tournament", getResult.getBody().getTournamentname()); // Check the updated name
// 	}

//     @Test
//     public void deleteTournament_ValidId_Success() throws Exception {
//         Tournament tournament = new Tournament();
//         tournament.setTournamentname("New York Tournament");
//         tournament.setLocation("New York");
//         tournament.setStartDate(LocalDate.parse("2024-09-20"));
//         tournament.setEndDate(LocalDate.parse("2024-09-21"));
//         tournament.setTime(LocalTime.parse("10:00:00"));
//         tournament.setStatus(Tournament.Status.UPCOMING);
//         tournament.setDescription("Test Tournament");
//         tournament.setWinnerId(null);
//         tournament.setPlayers(Arrays.asList(3L, 4L));
//         Tournament savedTournament = tournamentRepository.save(tournament);

//         URI uri = new URI(baseUrl + port + "/tournaments/" + savedTournament.getId());

//         // Change expected status code to 200
//         ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class);
//         assertEquals(200, result.getStatusCode().value());

//         // // Confirm tournament was deleted by expecting a 404
//         // ResponseEntity<Tournament> getResult = restTemplate.getForEntity(uri, Tournament.class);
//         // assertEquals(404, getResult.getStatusCode().value());
//     }

//     @Test
//     public void deleteTournament_InvalidId_Failure() throws Exception {
//         URI uri = new URI(baseUrl + port + "/tournaments/999");

//         ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class);

//         assertEquals(404, result.getStatusCode().value());
//     }
// }
