package csd.cuemaster.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;



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
    public String addPlayerUser(@Valid @RequestBody User user) {
        User savedUser = userService.addPlayer(user);
        if (savedUser == null) {
            return "Account Exists";
        }
        return "Registration Successful";
        // if (users.findByUsername(user.getUsername()) != null) {
        //     return "Account Exists";

        // }
        // user.setUsername(user.getUsername());
        // user.setPassword(encoder.encode(user.getPassword()));
        // user.setAuthorities("ROLE_PLAYER");
        // user.setProvider("normal");
        // users.save(user);
        // return "Registration Successful";
    }

    @PostMapping("/register/organiser")
    public String addOrganiserUser(@Valid @RequestBody User user) {
        User savedUser = userService.addOrganiser(user);
        if (savedUser == null) {
            return "Account Exists";
        }
        return "Registration Successful";

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