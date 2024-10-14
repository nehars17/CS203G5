package csd.cuemaster.user;

import java.util.Collections;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        List<GrantedAuthority> roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER"));
        
        session.setAttribute("role", roles);
        return "login"; // This will map to login.html in templates
    }

    @GetMapping("/googlelogin/organiser")
    public String organiserLogin(HttpSession session) {
        List<GrantedAuthority> roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        session.setAttribute("role", roles);
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
        List<GrantedAuthority> role = getRolesFromSession(session);

    
        String userEmail = userService.googleLogin(email,role);
        session.setAttribute("currentUser", userEmail);
        System.out.println("Logged in user: " + userEmail);

        return "index"; // Redirect to home page
        
    }

        // Method to safely retrieve roles from session
        @SuppressWarnings("unchecked")
        private List<GrantedAuthority> getRolesFromSession(HttpSession session) {
            Object roles = session.getAttribute("role");
            if (roles instanceof List) {
                return (List<GrantedAuthority>) roles; // Safe cast after checking
            }
            return Collections.emptyList(); // Return an empty list if roles are not present or not of the expected type
        }
}


