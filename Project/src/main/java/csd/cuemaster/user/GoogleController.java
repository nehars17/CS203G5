package csd.cuemaster.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import csd.cuemaster.services.JwtService;
import jakarta.servlet.http.HttpSession;

/**
 * GoogleController handles Google login requests and manages user authentication.
 * It provides an endpoint for users to log in using their Google account.
 * 
 * <p>This controller uses {@link UserService} to validate or register the user
 * and {@link JwtService} to generate a JWT token for authenticated users.</p>
 * 
 * <p>Endpoint:</p>
 * <ul>
 *   <li>POST /googlelogin - Handles Google login requests</li>
 * </ul>
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>{@link UserService} - Service for user operations</li>
 *   <li>{@link JwtService} - Service for JWT token operations</li>
 * </ul>
 * 
 * <p>Request Body:</p>
 * <ul>
 *   <li>email - The email of the user logging in with Google</li>
 *   <li>role - The role of the user logging in with Google</li>
 * </ul>
 * 
 * <p>Response:</p>
 * <ul>
 *   <li>On success: HTTP 200 OK with a JSON body containing a message, JWT token, and user role</li>
 *   <li>On failure: HTTP 401 Unauthorized with a JSON body containing an error message</li>
 * </ul>
 * 
 * 
 * @see UserService
 * @see JwtService
 */
@RestController
@RequestMapping("/googlelogin") // Set the path for this controller
public class GoogleController {

    @Autowired
    private UserService userService;



    @Autowired
    private JwtService jwtService;

    
   
    /**
     * Handles Google login requests.
     *
     * @param session the HTTP session
     * @param payload a map containing the user's email and role
     * @return a ResponseEntity containing a map with a success message, JWT token, and user role,
     *         or an error message if authentication fails
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleGoogleLogin(HttpSession session,
            @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String role = payload.get("role");
        Map<String, Object> errorResponse = new HashMap<>();
        try {
            System.out.println(role);
            // Validate or register the user using Google login
            User googleUser = userService.googleLogin(email, role);
            if(googleUser==null){
                errorResponse.put("message", "Please Register First");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Authenticate the user within Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    googleUser.getUsername(), null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Generate a JWT token for the user
            String userRole = googleUser.getAuthorities().iterator().next().getAuthority();

            String jwtToken = jwtService.generateToken(googleUser,googleUser.getId(),userRole);
            System.out.println("Generated JWT: " + jwtToken);
            // Get the user's role
            System.out.println(userRole);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("token", jwtToken);
            response.put("role", userRole);
            response.put("userId", googleUser.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            // Return an error response
            errorResponse.put("message", "Authentication failed!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
