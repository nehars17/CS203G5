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
            Profile profile2 = new Profile( "admin", "admin",LocalDate.parse("2000-01-01"), "Singapore", "ProfilePhoto_123.jpg",user);
            users.save(user);
            profiles.save(profile2);
            for (int i=1;i<=32;i++){
                User user4 = new User("player"+i+"@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal", true);
                users.save(user4);
                Profile profile1 = new Profile("Player"+i, "lastname",LocalDate.parse("2000-01-01"), "Singapore", "ProfilePhoto_123.jpg",user4);
                profiles.save(profile1);
            }
            System.out.println("[Add user]: " + user.getUsername());
        } else {
            System.out.println("[User exists]: " + adminEmail);
        }

        
    }
}