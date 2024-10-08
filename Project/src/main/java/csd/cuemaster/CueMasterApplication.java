package csd.cuemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootApplication
public class CueMasterApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CueMasterApplication.class, args);
        
        seedData(context);  // Optional: seed initial data
    }

    private static void seedData(ApplicationContext context) {
        UserRepository userRepository = context.getBean(UserRepository.class);
        //TournamentRepository tournamentRepository = context.getBean(TournamentRepository.class);
        BCryptPasswordEncoder passwordEncoder = context.getBean(BCryptPasswordEncoder.class);
        //RestTemplate restTemplate = context.getBean(RestTemplate.class); // Ensure RestTemplate bean is configured
        
        // // Add admin user
        // User admin = new User("admin", passwordEncoder.encode("goodpassword"), "ROLE_ADMIN");
        // userRepository.save(admin);
        // System.out.println("[Add user]: " + admin.getUsername());

        System.out.println("[Add user]: " + userRepository.save(
            new User("admin", passwordEncoder.encode("goodpassword"), "ROLE_ADMIN")).getUsername());

        
        // System.out.println("[Add tournament]: " + tournamentRepository.save(
        //     new Tournament("tournamentName", LocalDate.now(), LocalTime.now(), "location", "status")
        //     ).getTournamentName());

        // System.out.println("[Add user1]: " + userRepository.save(
        //     new User("user1", passwordEncoder.encode("goodpassword1"), "ROLE_PLAYER")).getUsername()
        // );

        // System.out.println("[Add user2]: " + userRepository.save(
        //     new User("user2", passwordEncoder.encode("goodpassword2"), "ROLE_PLAYER")).getUsername()
        // );
        // // Create and save the tournament
        // Tournament tournament = new Tournament("Fall Tournament", LocalDate.of(2024, 10, 15),
        //         LocalTime.of(14, 30), "Community Center", "Upcoming");
        // tournamentRepository.save(tournament); // Save tournament to DB
        
        // Create and save users
        // User user1 = new User("player1", passwordEncoder.encode("password1"), "ROLE_PLAYER");
        // User user2 = new User("player2", passwordEncoder.encode("password2"), "ROLE_PLAYER");
        
        // userRepository.save(user1);
        // userRepository.save(user2);

        // Long tID = tournament.getId();

        // // Create the match object
        // Match match = new Match(tournament, user1, user2, LocalDate.now(), LocalTime.now(), 10, 10);
        // //matchRepository.save(match);

        // // Set up headers
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        
        // // Create the request entity
        // HttpEntity<Match> requestEntity = new HttpEntity<>(match, headers);

        // // Send the POST request
        // ResponseEntity<Match> response = restTemplate.postForEntity("http://localhost:8080/matches/create", requestEntity, Match.class);

        // if (response.getStatusCode().is2xxSuccessful()) {
        //     System.out.println("Match created successfully: " + response.getBody());
        // } else {
        //     System.out.println("Failed to create match: " + response.getStatusCode());
        // }
    }
}
    // private static void seedData(ApplicationContext context) {
    //     UserRepository userRepository = context.getBean(UserRepository.class);

    //     BCryptPasswordEncoder passwordEncoder = context.getBean(BCryptPasswordEncoder.class);
        
    //     System.out.println("[Add user]: " + userRepository.save(
    //     new User("admin", passwordEncoder.encode("goodpassword"), "ROLE_ADMIN")).getUsername());

    //             // Create the tournament and users
    //     Tournament tournament = new Tournament("Fall Tournament", LocalDate.of(2024, 10, 15),
    //             LocalTime.of(14, 30), "Community Center", "Upcoming");
        
    //     User user1 = new User("player1", "password1", "ROLE_PLAYER");
    //     User user2 = new User("player2", "password2", "ROLE_PLAYER");

    //     // Create the match object
    //     Match match = new Match();
    //     match.setTournament(tournament);
    //     match.setUser1(user1);
    //     match.setUser2(user2);
    //     match.setMatchDate(LocalDate.now());
    //     match.setMatchTime(LocalTime.now());

    //     // Set up headers
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
        
    //     // Create the request entity
    //     HttpEntity<Match> requestEntity = new HttpEntity<>(match, headers);

    //     // Send the POST request
    //     ResponseEntity<Match> response = restTemplate.postForEntity("http://localhost:8080/matches/create", requestEntity, Match.class);

    //     if (response.getStatusCode().is2xxSuccessful()) {
    //         System.out.println("Match created successfully: " + response.getBody());
    //     } else {
    //         System.out.println("Failed to create match: " + response.getStatusCode());
    //     }
    // }
 
    //     //admin.setRole("ROLE_ADMIN");

    //     // Save to DB
    // }
