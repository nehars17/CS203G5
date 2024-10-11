package csd.cuemaster.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public User addPlayerUser(@Valid @RequestBody User user,HttpServletRequest request) {
        User savedUser = userService.addPlayer(user);
        if (savedUser == null) {
            throw new UserExistsException(user.getUsername());
            
        }
        String activationLink = "http://localhost:8080/activate?token=" + savedUser.getActivationToken();
        try {
            emailService.sendActivationEmail(savedUser.getUsername(), activationLink);
        } catch (MessagingException e) {
            throw new AccountActivationException("unable to send email");
        }
        return savedUser;

    }

    @PostMapping("/register/organiser")
    public User addOrganiserUser(@Valid @RequestBody User user, HttpServletRequest request) {
        User savedUser = userService.addOrganiser(user);
        
        if (savedUser == null) {
            throw new UserExistsException(user.getUsername());

        }
        String activationLink = "http://localhost:8080/activate?token=" + savedUser.getActivationToken();
        try {
            emailService.sendActivationEmail(savedUser.getUsername(), activationLink);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return savedUser;

    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token) {
        String message = userService.accountActivation(token);
        return message; // Return a view to show the activation status
    }

    @GetMapping("/normallogin")
    public User retrieveUser(HttpSession session, @Valid @RequestBody User user) {
        String existingSession = (String) session.getAttribute("currentUser");
        if(existingSession!=null){
            throw new UserSessionExistException("Please Logout First");
        }
        User LoggedInUser = userService.loginUser(user);
        if (LoggedInUser==null) {
            throw new UsernameNotFoundException("Username or Password Incorrect");

        }
        if(!LoggedInUser.isEnabled()){
            throw new AccountActivationException("Please activate account first");
        }
        if(!LoggedInUser.getProvider().equals("google")){
        } else {
            throw new UsernameNotFoundException("Please login using Google");
        }
   
        session.setAttribute("currentUser", LoggedInUser.getUsername());
        return LoggedInUser;

       
    }
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // Invalidate the session to log the user out
        session.setAttribute("currentUser", null);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/user/{user_id}/account")
    public void deleteAccount(@PathVariable (value = "user_id") Long user_id){
        userService.deleteUser(user_id);
    }

}