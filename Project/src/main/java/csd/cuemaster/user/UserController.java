package csd.cuemaster.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

import csd.cuemaster.imageservice.ImageService;
import csd.cuemaster.services.EmailService;
import csd.cuemaster.services.JwtService;
import jakarta.mail.MessagingException;
import jakarta.persistence.ElementCollection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * UserController is a REST controller that handles user-related operations such
 * as registration, authentication,
 * account activation, password management, and account management.
 * 
 * It provides endpoints for:
 * - Retrieving the list of users
 * - Retrieving the details of the currently authenticated user
 * - Registering a new user account
 * - Activating a user account
 * - Authenticating a user using normal login and two-factor authentication
 * - Logging out a user
 * - Handling forgot password functionality
 * - Resetting a user's password
 * - Updating a user's password
 * - Deleting a user's account
 * - Unlocking a user's account
 * 
 * The controller uses various services such as UserService, EmailService,
 * JwtService, and RestTemplate to perform
 * the necessary operations.
 * 
 * It also verifies reCAPTCHA tokens using the Google reCAPTCHA API to enhance
 * security.
 * 
 * The controller is annotated with @RestController, indicating that it is a
 * Spring MVC controller where every
 * method returns a domain object instead of a view.
 * 
 * Dependencies are injected using the @Autowired annotation.
 * 
 * The @Value annotation is used to inject the reCAPTCHA client secret key from
 * the application properties.
 * 
 * The controller handles various exceptions and returns appropriate HTTP
 * responses.
 */

@RestController
@Configuration
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Value("${captcha.client-secret}")
    private String captchasecretkey;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ImageService ImageService;
    @Lazy
    @Autowired
    private RestTemplate restTemplate;



    @Value("${captcha.enabled}") 
    private boolean captchaEnabled;

    @ElementCollection
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.listUsers();
    }

    @GetMapping("/Provider/{user_id}")
    public String getUserProvider(@PathVariable(value = "user_id") Long user_id){

        return userService.getProvider(user_id);
    }

    @GetMapping("/user/{user_id}")
    public User getUserByUserId(@PathVariable(value = "user_id") Long user_id) {
        return userService.getUser(user_id);
    }

    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*
     *
     * Retrieves the details of the currently authenticated user.
     * 
     */
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

    /*
     * Registers a new user account and sends an activation email.
     * * @param user the user object containing the registration details
     * * @param request the HTTP request
     * * @return a ResponseEntity containing the saved user object
     * * @throws Exception if an error occurs during the registration process
     */
    @PostMapping("/register")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user,
            HttpServletRequest request) throws Exception {

        // Verify reCAPTCHA token

        if (captchaEnabled && !verifyRecaptcha((String) user.getRecaptchaToken())) {
            throw new IllegalArgumentException("Invalid CAPTCHA, please try again");
        }

        user.setRecaptchaToken(null);

        User savedUser = userService.addUser(user);
        if (savedUser == null) {
            throw new UserExistsException(user.getUsername());
        }

        try {
            emailService.sendActivationEmail(savedUser.getUsername(), savedUser.getTotpToken().getCode());
        } catch (MessagingException e) {
            throw new AccountActivationException("Unable to send email");
        }

        return ResponseEntity.ok(savedUser);
    }

    /**
     * Verifies the reCAPTCHA token using the Google reCAPTCHA API.
     *
     * @param recaptchaToken the reCAPTCHA token to verify
     * @return true if the token is valid, false otherwise
     */
    public boolean verifyRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", captchasecretkey);
        params.add("response", recaptchaToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // RestTemplate instance is now injected

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            String responseBody = response.getBody();

            if (responseBody != null) {
                // Use JSONParser to parse the response into a JSONObject
                JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);

                // Access the "success" key in the response
                boolean success = Boolean.parseBoolean(jsonResponse.get("success").toString());
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

    /**
     * Activates a user account using the provided token.
     *
     * @param payload a map containing the activation token
     * @return a ResponseEntity with a message indicating the result of the
     *         activation
     * @throws Exception if an error occurs during the activation process
     */
    @PostMapping("/activate")
    public ResponseEntity<String> activateAccount(@Valid @RequestBody Map<String, Object> payload) throws Exception {
        String token = (String) payload.get("token");
        String message = userService.accountActivation(token);
        return ResponseEntity.ok(message); // Return a ResponseEntity with the activation status message
    }

    /**
     * Authenticates a user using normal login and verifies reCAPTCHA token.
     * 
     * @param session the HTTP session
     * @param user    the user object containing login credentials
     * @return a ResponseEntity containing user details and role if login is
     *         successful,
     *         or an appropriate message if login fails
     * @throws Exception if an error occurs during the login process
     */
    @PostMapping("/normallogin")
    public ResponseEntity<Map<String, Object>> retrieveUser(HttpSession session, @Valid @RequestBody User user)
            throws Exception {

        // Verify reCAPTCHA token
        if (captchaEnabled && !verifyRecaptcha((String) user.getRecaptchaToken())) {
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
            try {
                emailService.sendActivationEmail(loggedInUser.getUsername(), loggedInUser.getTotpToken().getCode());
            } catch (MessagingException e) {
                throw new AccountActivationException("Unable to send email");
            }
            return ResponseEntity.ok(response);
        }
        if (!loggedInUser.isUnlocked()) {
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

    /*
     * Authenticates a user using two-factor authentication.
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyTwoFactorCode(@Valid @RequestBody Map<String, Object> payload)
            throws Exception {
        // Extract user object and code from payload
        User userDetails = new ObjectMapper().convertValue(payload.get("user"), User.class);
        String code = (String) payload.get("code");
        String role = (String) payload.get("role");
        User loggedInUser = userService.EmailAuth(code, userDetails.getUsername());
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session

        // Clear the authentication context
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully!");
    }

    /**
     * Handles the forgot password functionality.
     *
     * @param session the HTTP session
     * @param payload a map containing the username of the user who forgot their
     *                password
     * @return a ResponseEntity with a message indicating whether the password reset
     *         link was sent or the user was not found
     * @throws Exception if an error occurs during the process
     */
    @PutMapping("/forgotPassword")
    public ResponseEntity<String> ForgotPassword(
            HttpSession session,
            @Valid @RequestBody Map<String, Object> payload) throws Exception {
        String username = (String) payload.get("username");
        User foundUser = userService.forgotPassword(username);

        if (foundUser != null) {
            emailService.sendPasswordResetEmail(username, foundUser.getId(), foundUser.getTotpToken().getCode());
            return ResponseEntity.ok("Password reset link has been sent to your email.");

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    /**
     * Resets the password of the specified user.
     * public ResponseEntity<String> resetPassword(@Valid @RequestBody Map<String,
     * Object> payload) throws Exception {
     *
     * @param payload a map containing the token, new password, and user ID
     * @return a ResponseEntity with a message indicating the result of the password
     *         reset
     * @throws Exception if an error occurs during the process
     */

    @PutMapping("/resetPassword")
    public ResponseEntity<String> resettingPassword(@Valid @RequestBody Map<String, Object> payload) throws Exception {
        String token = (String) payload.get("token");
        String newPassword = (String) payload.get("password");
        String user_id = (String) payload.get("user_id");
        Long id = Long.valueOf(user_id);
        String message = userService.resetPassword(id, newPassword, token);
        System.out.println(message);
        return ResponseEntity.ok(message);
    }

    /*
     * Updates the password of the specified user.
     *
     * @param user_id the ID of the user whose password is to be updated
     * 
     * @param user the user object containing the new password
     */
    @PutMapping("/update/{user_id}/password")
    public void updatePassword(@PathVariable(value = "user_id") Long user_id, @Valid @RequestBody User user) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId() == user_id) {
            userService.updatePassword(user_id, user);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this password");
        }
    }

    /**
     * Deletes the account of the specified user.
     *
     * @param user_id the ID of the user whose account is to be deleted
     * @throws ResponseStatusException if the current user is not allowed to delete
     *                                 the account
     */
    @DeleteMapping("/user/{user_id}/account")
    public void deleteAccount(@PathVariable(value = "user_id") Long user_id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId() == 1 || currentUser.getId() == user_id) {
            userService.deleteUser(user_id);
            ImageService.deleteImage("ProfilePhoto_" + user_id + ".jpg");
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this account");
        }
    }

    /**
     * Unlocks the account of the specified user.
     *
     * @param user_id the ID of the user whose account is to be unlocked
     * @throws MessagingException
     * @throws ResponseStatusException if the current user is not allowed to unlock
     *                                 the account
     */
    @PutMapping("/user/{user_id}/account")
    public void unlockAccount(@PathVariable(value = "user_id") Long user_id) throws MessagingException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = currentUser.getAuthorities().iterator().next().getAuthority();

        if (role == "ROLE_ADMIN") {
            userService.unlockAccount(user_id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this account");
        }
    }
}