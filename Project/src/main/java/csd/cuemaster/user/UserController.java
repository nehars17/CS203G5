package csd.cuemaster.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import csd.cuemaster.services.EmailService;
import csd.cuemaster.services.JwtService;
import jakarta.mail.MessagingException;
import jakarta.persistence.ElementCollection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Value("${captcha.client-secret}")
    private String captchasecretkey;

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
     * @throws Exception
     */

    @PostMapping("/register")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user,
            HttpServletRequest request) throws Exception {

        // Verify reCAPTCHA token
        if (!verifyRecaptcha((String) user.getRecaptchaToken())) {
            throw new IllegalArgumentException("Invalid CAPTCHA, please try again");
        }

        user.setRecaptchaToken(null);

        User savedUser = userService.addUser(user);
        if (savedUser == null) {
            throw new UserExistsException(user.getUsername());
        }

        String activationLink = "http://localhost:3000/activateaccount?token=" + savedUser.getTotpToken().getCode();
        try {
            emailService.sendActivationEmail(savedUser.getUsername(), activationLink);
        } catch (MessagingException e) {
            throw new AccountActivationException("Unable to send email");
        }

        return ResponseEntity.ok(savedUser);
    }

    public boolean verifyRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", captchasecretkey);
        params.add("response", recaptchaToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            String responseBody = response.getBody();

            if (responseBody != null) {
                // Use JSONParser to parse the response into a JSONObject
                JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);

                // Access the "success" key in the response
                boolean success = (boolean) jsonResponse.get("success");
                return success;
            } else {
                System.err.println("No response from reCAPTCHA API.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error while verifying reCAPTCHA: " + e.getMessage());
            return false;
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateAccount(@Valid @RequestBody Map<String, Object> payload) throws Exception {
        String token = (String) payload.get("token");
        String message = userService.accountActivation(token);
        return ResponseEntity.ok(message); // Return a view to show the activation status
    }

    @PostMapping("/normallogin")
    public ResponseEntity<Map<String, Object>> retrieveUser(HttpSession session, @Valid @RequestBody User user)
            throws Exception {

        // Verify reCAPTCHA token
        if (!verifyRecaptcha((String) user.getRecaptchaToken())) {
            throw new IllegalArgumentException("Invalid CAPTCHA, please try again");
        }
        user.setRecaptchaToken(null);
        User loggedInUser = userService.loginUser(user);
        Map<String, Object> response = new HashMap<>();
        if (loggedInUser == null) {
            throw new UsernameNotFoundException("Username or Password Incorrect");
        }
        if (!loggedInUser.isEnabled()) {
            response.put("message", "Please activate account, check email");
            String activationLink = "http://localhost:3000/activateaccount?token="
                    + loggedInUser.getTotpToken().getCode();
            try {
                emailService.sendActivationEmail(loggedInUser.getUsername(), activationLink);
            } catch (MessagingException e) {
                throw new AccountActivationException("Unable to send email");
            }
            return ResponseEntity.ok(response);
        }
        if (!loggedInUser.isUnlocked()) {
            System.out.println("IMCALLED");
            response.put("message", "Account Locked, Please contact administrator at cuemasternoreply@gmail.com");
            return ResponseEntity.ok(response);
        }
        if (loggedInUser.getProvider().equals("google")) {
            throw new UsernameNotFoundException("Please login using Google");
        }

        try {
            emailService.send2FAEmail(loggedInUser.getUsername(), loggedInUser.getTotpToken().getCode());
        } catch (MessagingException e) {
            throw new AccountActivationException("unable to send email");
        }

        String role = loggedInUser.getAuthorities().iterator().next().getAuthority();

        response.put("user", loggedInUser);
        response.put("role", role);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody Map<String, Object> payload)
            throws Exception {
        // Extract user object and code from payload
        User userDetails = new ObjectMapper().convertValue(payload.get("user"), User.class);
        String code = (String) payload.get("code");
        String role = (String) payload.get("role");
        User loggedInUser = userService.EmailAuth(code, userDetails.getUsername());
        System.out.println(loggedInUser);
        if (loggedInUser != null) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loggedInUser.getUsername(), null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            String jwtToken = jwtService.generateToken(loggedInUser, loggedInUser.getId(), role);
            Map<String, Object> response = new HashMap<>();
            response.put("user", loggedInUser);
            response.put("token", jwtToken);
            response.put("role", role);
            System.out.println(role);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid code, please try again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok("Logged out successfully!");
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<String> ForgotPassword(HttpSession session, @Valid @RequestBody Map<String, Object> payload)
            throws Exception {
        String username = (String) payload.get("username");

        User foundUser = userService.forgotPassword(username);
        System.out.println(foundUser);
        if (foundUser != null) {
            emailService.sendPasswordResetEmail(username, foundUser.getId(), foundUser.getTotpToken().getCode());
            return ResponseEntity.ok("Password reset link has been sent to your email.");

        }

        return ResponseEntity.ok("User not found");
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<String> ResetPassword(@Valid @RequestBody Map<String, Object> payload) throws Exception {
        String token = (String) payload.get("token");
        String newPassword = (String) payload.get("password");
        String user_id = (String) payload.get("user_id");
        Long id = Long.valueOf(user_id);
        String message = userService.resetPassword(id, newPassword, token);
        return ResponseEntity.ok(message);

    }

    @PutMapping("/update/{user_id}/password")
    public void updatePassword(@PathVariable(value = "user_id") Long user_id, @Valid @RequestBody User user) {
        userService.updatePassword(user_id, user);

    }

    @DeleteMapping("/user/{user_id}/account")
    public void deleteAccount(@PathVariable(value = "user_id") Long user_id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId() == 1 || currentUser.getId() == user_id) {
            userService.deleteUser(user_id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this account");

        }
    }

    @PutMapping("/user/{user_id}/account")
    public void unlockAccount(@PathVariable(value = "user_id") Long user_id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId() == 1 || currentUser.getId() == user_id) {
            userService.unlockAccount(user_id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this account");

        }

    }

}