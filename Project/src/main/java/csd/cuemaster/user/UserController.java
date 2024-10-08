package csd.cuemaster.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;



    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.listUsers();
    }

    /**
     * Using BCrypt encoder to encrypt the password for storage
     * 
     * @param user
     * @return
     */

    @PostMapping("/register/player")
    public String addPlayerUser(@Valid @RequestBody User user,HttpServletRequest request) {
        User savedUser = userService.addPlayer(user);
        if (savedUser == null) {
            return "Account Exists";
        }
        String activationLink = "http://localhost:8080/activate?token=" + savedUser.getActivationToken();
        try {
            emailService.sendActivationEmail(savedUser.getUsername(), activationLink);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "Registration Successful. Please check your email to activate your account.";

    }

    @PostMapping("/register/organiser")
    public String addOrganiserUser(@Valid @RequestBody User user, HttpServletRequest request) {
        User savedUser = userService.addOrganiser(user);
        
        if (savedUser == null) {
            return "Account Exists";
        }
        String activationLink = "http://localhost:8080/activate?token=" + savedUser.getActivationToken();
        try {
            emailService.sendActivationEmail(savedUser.getUsername(), activationLink);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "Registration Successful. Please check your email to activate your account.";

    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token) {
        String message = userService.accountActivation(token);

        return message; // Return a view to show the activation status
    }

    @GetMapping("/normallogin")
    public String retrieveUser(HttpSession session, @Valid @RequestBody User user) {
        String existingSession = (String) session.getAttribute("currentUser");
        if(existingSession!=null){
            return "Please logout first";
        }
        User LoggedInUser = userService.loginUser(user);
        if (LoggedInUser==null) {
            return "Username or Password Incorrect";

        }
        if(!LoggedInUser.isEnabled()){
            return "Please activate account first";
        }
        if(!LoggedInUser.getProvider().equals("google")){
        } else {
            throw new UsernameNotFoundException("Please login using Google");
        }
   
        session.setAttribute("currentUser", LoggedInUser.getUsername());
        return "Login Successful";

       
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate the session to log the user out
        session.setAttribute("currentUser", null);
        return "Logged out successfully";
    }

}