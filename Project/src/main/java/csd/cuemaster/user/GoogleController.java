package csd.cuemaster.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class GoogleController {
   
    @Autowired
    private UserService userService;

    

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

 
    String userEmail = userService.googleLogin(email,role);
    session.setAttribute("currentUser", userEmail);
    System.out.println("Logged in user: " + userEmail);

    return "index"; // Redirect to home page
    
}
}


