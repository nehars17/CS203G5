package csd.cuemaster.user;

import java.security.cert.LDAPCertStoreParameters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import csd.cuemaster.services.EmailService;
import csd.cuemaster.services.JwtService;
import jakarta.mail.MessagingException;
import jakarta.persistence.ElementCollection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @ElementCollection
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.listUsers();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof User)) {
            throw new UsernameNotFoundException("User not found");
        }
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("username", currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Using BCrypt encoder to encrypt the password for storage
     * 
     * @param user
     * @return
     */

    @PostMapping("/register")
    public User addUser(@Valid @RequestBody User user, HttpServletRequest request) {
        User savedUser = userService.addUser(user);
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

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token) {
        String message = userService.accountActivation(token);
        return message; // Return a view to show the activation status
    }

    @PostMapping("/normallogin")
    public ResponseEntity<Map<String, Object>> retrieveUser(HttpSession session, @Valid @RequestBody User user) {
        User loggedInUser = userService.loginUser(user);
        if (loggedInUser == null) {
            throw new UsernameNotFoundException("Username or Password Incorrect");
        }
        if (!loggedInUser.isEnabled()) {
            throw new AccountActivationException("Please activate account first");
        }
        if (loggedInUser.getProvider().equals("google")) {
            throw new UsernameNotFoundException("Please login using Google");
        }

        try {
            emailService.send2FAEmail(loggedInUser.getUsername(), loggedInUser.getAuthCode());
        } catch (MessagingException e) {
            throw new AccountActivationException("unable to send email");
        }

        String role = loggedInUser.getAuthorities().iterator().next().getAuthority();

        Map<String, Object> response = new HashMap<>();
        response.put("user", loggedInUser);
        response.put("role", role);
        return ResponseEntity.ok(response);

    }

   @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody Map<String, Object> payload) {
    // Extract user object and code from payload
    System.out.println(payload.get("user"));
    User loggedInUser = new ObjectMapper().convertValue(payload.get("user"), User.class);
    String code = (String) payload.get("code");
    String role = (String) payload.get("role");
    System.out.println(loggedInUser.getId());
    boolean isValid = userService.EmailAuth(loggedInUser.getUsername(), code);
    if (isValid) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loggedInUser.getUsername(),
                        "goodpassword"
                )
        );

        // String role = loggedInUser.getAuthorities().iterator().next().getAuthority();

        String jwtToken = jwtService.generateToken(loggedInUser, loggedInUser.getId(), role);
        System.out.println(jwtToken);
        Map<String, Object> response = new HashMap<>();
        response.put("user", loggedInUser);
        response.put("token", jwtToken);
        response.put("role", role);

        return ResponseEntity.ok(response);
    } 
    else {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid code, please try again.");
        System.out.println("IM CALLED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok("Logged out successfully!");
    }

    @DeleteMapping("/user/{user_id}/account")
    public void deleteAccount(@PathVariable(value = "user_id") Long user_id) {
        userService.deleteUser(user_id);
    }

}