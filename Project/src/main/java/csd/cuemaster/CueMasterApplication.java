package csd.cuemaster;

import java.time.LocalDate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootApplication
public class CueMasterApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(CueMasterApplication.class, args);
        UserRepository users = ctx.getBean(UserRepository.class);
        ProfileRepository profiles = ctx.getBean(ProfileRepository.class);
        BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);

        // Check if the admin user already exists
        String adminEmail = "cuemasternoreply@gmail.com";
        if (!users.findByUsername(adminEmail).isPresent()) {
            User user = new User("cuemasternoreply@gmail.com" , encoder.encode("goodpassword"), "ROLE_ADMIN", "normal", true);
            System.out.println("[Add user]: " + user.getUsername());
        } else {
            System.out.println("[User exists]: " + adminEmail);
        }

        
    }
}