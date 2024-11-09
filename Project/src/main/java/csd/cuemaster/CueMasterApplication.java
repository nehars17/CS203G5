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
        ApplicationContext ctx = SpringApplication.run(CueMasterApplication.class, args);
        UserRepository users = ctx.getBean(UserRepository.class);
        BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);

        // Check if the admin user already exists
        String adminEmail = "admin@gmail.com";
        if (!users.findByUsername(adminEmail).isPresent()) {
            User user = new User("cuemasternoreply@gmail.com" , encoder.encode("goodpassword"), "ROLE_ADMIN", "normal", true);
            User user2 = new User("org@gmail.com", encoder.encode("goodpassword"), "ROLE_ORGANISER", "normal", true);
            User user3 = new User("nehars.rs@gmail.com", encoder.encode("goodpassword"), "ROLE_PLAYER", "normal", true);
            users.save(user);
            users.save(user2);
            users.save(user3);
            System.out.println("[Add user]: " + user.getUsername());
        } else {
            System.out.println("[User exists]: " + adminEmail);
        }

        
    }
}