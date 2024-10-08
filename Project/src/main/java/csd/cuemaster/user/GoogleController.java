package csd.cuemaster.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class GoogleController {
    // private UserRepository users;
    // private BCryptPasswordEncoder encoder;
    @Autowired
    private UserService userService;

    // public GoogleController(UserRepository users, BCryptPasswordEncoder encoder) {
    //     this.users = users;
    //     this.encoder = encoder;
    // }

    @GetMapping("/googlelogin/player")
    public String playerLogin(HttpSession session) {
        session.setAttribute("role", "ROLE_PLAYER");
        return "login"; // This will map to login.html in templates
    }

    @GetMapping("/googlelogin/organiser")
    public String organiserLogin(HttpSession session) {
        session.setAttribute("role", "ROLE_ORGANISER");
        return "login"; // This will map to login.html in templates
    }

    
    @GetMapping("/loginSuccess")
public String onPlayerAuthenticationSuccess(HttpSession session,HttpServletRequest request) {
    System.out.println("IM CALLED");
    String existingSession = (String) session.getAttribute("currentUser");
    System.out.println("session exist" + existingSession);
    if (existingSession != null) {
        return "loginError"; // Prevent multiple logins
    }

    String email = (String) request.getSession().getAttribute("userEmail");
    String role = (String) session.getAttribute("role");

    // Optional<User> existingUser = users.findByUsername(email);
    // System.out.println("User exist "+existingUser);
    String userEmail = userService.googleLogin(email,role);
    session.setAttribute("currentUser", userEmail);
    System.out.println("Logged in user: " + userEmail);

    return "index"; // Redirect to home page
    
}

// @GetMapping("/loginSuccess/organiser")
// public String onOrganiserAuthenticationSuccess(HttpSession session,HttpServletRequest request) {
//     System.out.println("IM CALLED");
//     String existingSession = (String) session.getAttribute("currentUser");
//     System.out.println("session exist" + existingSession);
//     if (existingSession != null) {
//         return "loginError"; // Prevent multiple logins
//     }

//     String email = (String) request.getSession().getAttribute("userEmail");

//     Optional<User> existingUser = users.findByUsername(email);
//     System.out.println("User exist "+existingUser);

//     if(email!=null && existingUser.isEmpty() && existingSession==null){
//         User newUser = new User(email, "no password", "ROLE_ORGANISER","google");
//         users.save(newUser);
//         System.out.println("New user created: " + email);
//     }
//     session.setAttribute("currentUser", email);
//     System.out.println("Logged in user: " + email);

//     return "index"; // Redirect to home page
    
// }
}


