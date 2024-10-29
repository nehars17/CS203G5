package csd.cuemaster.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import csd.cuemaster.services.JwtService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/googlelogin") // Set the path for this controller
public class GoogleController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository users;

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleGoogleLogin(HttpSession session,
            @RequestBody Map<String, String> payload) {
        String tokenId = payload.get("tokenId");
        String email = payload.get("email");
        String role = payload.get("role");
        System.out.println("Google login attempt");
        try {
            System.out.println(role);
            // Validate or register the user using Google login
            User googleUser = userService.googleLogin(email, role);

            if(googleUser==null){
                throw new UsernameNotFoundException("Please Register First");
            }

            // Authenticate the user within Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    googleUser.getUsername(), null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Generate a JWT token for the user
            String jwtToken = jwtService.generateToken(googleUser,googleUser.getId());
            System.out.println("Generated JWT: " + jwtToken);
            // Get the user's role
            String userRole = googleUser.getAuthorities().iterator().next().getAuthority();
            System.out.println(userRole);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("token", jwtToken);
            response.put("role", userRole);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            // Return an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Authentication failed!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
