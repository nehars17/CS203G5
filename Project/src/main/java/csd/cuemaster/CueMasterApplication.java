package csd.cuemaster;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
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
            User user = new User("admin@gmail.com", encoder.encode("goodpassword"), "ROLE_ADMIN", "normal", true);
            
            users.save(user);
            System.out.println("[Add user]: " + user.getUsername());
        } else {
            System.out.println("[User exists]: " + adminEmail);
        }

        // Uncomment the following lines if you want to add an organizer user as well
        // String orgEmail = "org@gmail.com";
        // if (!users.findByUsername(orgEmail).isPresent()) {
        //     List<SimpleGrantedAuthority> orgAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORGANISER"));
        //     User orgUser = new User(orgEmail, encoder.encode("goodpassword"), orgAuthorities, "normal", true);
        //     users.save(orgUser);
        //     System.out.println("[Add user]: " + orgUser.getUsername());
        // } else {
        //     System.out.println("[User exists]: " + orgEmail);
        // }
    }
}
