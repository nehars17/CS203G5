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
        		
        // JPA user repository init
        UserRepository users = ctx.getBean(UserRepository.class);
        BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
        System.out.println("[Add user]: " + users.save(
        new User("admin", encoder.encode("goodpassword"), "ROLE_ADMIN")).getUsername());

        // User user = new User("bryan", encoder.encode("goodpassword"), "ROLE_ORGANIZER");
        // User saveduser = users.save(user);
        // System.out.println("[Add user]: " + saveduser.getUsername());

        // String fos = "C:\\Users\\ngcho\\OneDrive\\Desktop\\sunset.jpg";
        // ProfileRepository profiles = ctx.getBean(ProfileRepository.class);
        // System.out.println( profiles.save(new Profile("Bryan","Ng",LocalDate.of(2000,6,9),"Singapore",fos,"SMU",saveduser)).getFirstname());
        // Use the FileOutputStream (e.g., write data to the file)
    }
    
}
