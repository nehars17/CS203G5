package csd.cuemaster;

import java.util.Arrays;

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
        // if (users.findByUsername("admin@gmail.com")==null){
        System.out.println("[Add user]: " + users.save(
            new User("admin@gmail.com", encoder.encode("goodpassword"), Arrays.asList("ROLE_ADMIN"),"normal",true)).getUsername());
        // }
        users.save(new User("org@gmail.com", encoder.encode("goodpassword"), Arrays.asList("ROLE_ORGANISER"),"normal",true));
    

    }
    
}
