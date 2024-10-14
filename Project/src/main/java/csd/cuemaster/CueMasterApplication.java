package csd.cuemaster;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;

@SpringBootApplication
public class CueMasterApplication {

    public static final String ROLE_PLAYER = "ROLE_PLAYER";
    public static final String ROLE_ORGANIZER = "ROLE_ORGANIZER";

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(CueMasterApplication.class, args);
        UserRepository users = ctx.getBean(UserRepository.class);
        BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
        
        // Check if the admin user already exists
        if (users.findByUsername("admin@gmail.com") == null) {
            User adminUser = new User("admin@gmail.com", encoder.encode("goodpassword"), 
                List.of(new SimpleGrantedAuthority(ROLE_PLAYER)), "normal", true);
            System.out.println("[Add user]: " + users.save(adminUser).getUsername());
        }
        
        // Create an organizer user
        users.save(new User("org@gmail.com", encoder.encode("goodpassword"), 
            List.of(new SimpleGrantedAuthority(ROLE_ORGANIZER)), "normal", true)); 
            
        
    }
}

