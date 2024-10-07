package csd.cuemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.client.RestTemplateClient;
import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class CueMasterApplication {

	public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(CueMasterApplication.class, args);
        TournamentRepository tournaments = ctx.getBean(TournamentRepository.class);

        // Create and save a tournament with valid initial data to satisfy validation constraints
        Tournament tournament = new Tournament("New York", LocalDate.now(), LocalDate.now().plusDays(5),
                LocalTime.of(12, 0), Tournament.Status.UPCOMING, "Annual 9-ball Tournament", null, new ArrayList<>());
        
        System.out.println("[Add tournament]: " + tournaments.save(tournament).getId());
    }
}
