package csd.cuemaster.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class GoogleController {
   
    @Autowired
    private UserService userService;

    

    @PostMapping("/googlelogin")
    public ResponseEntity<?> handleGoogleLogin(HttpSession session,@RequestBody Map<String, String> payload) {
        String tokenId = payload.get("tokenId");
        System.out.println("loggedIn");
        String email = payload.get("email");
        String role = payload.get("role");
        String userEmail = userService.googleLogin(email,role);
        session.setAttribute("currentUser", userEmail);
        // Handle the token and authentication logic here
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("email", email);
        response.put("role", role); // Example role

        return ResponseEntity.ok(response);
    }

    

    
}


