package csd.cuemaster.user;

import java.security.Key;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import csd.cuemaster.models.TOTPToken;
import csd.cuemaster.services.EmailService;
import csd.cuemaster.services.TOTPService;
import jakarta.mail.MessagingException;

/**
 * Service implementation for managing users.
 */

 
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository users;
    
    @Autowired
    private TOTPService totpService;

    @Autowired BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    /**
     * Lists all users.
     * 
     * @return a list of all users.
     */
    @Override
    public List<User> listUsers() {
        return users.findAll();
    }

    /**
     * Handles the forgot password process for a user.
     * 
     * @param username the username of the user who forgot their password.
     * @return the user with updated secret and TOTP token.
     * @throws Exception if the user is not found.
     */
    @Override
    public User forgotPassword(String username) throws Exception {
        User foundUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        foundUser.setSecret(secretKey);
        foundUser.setTotpToken(totpCode);
        System.out.println(foundUser.getSecret());
        users.save(foundUser);
        return foundUser;
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param id the ID of the user.
     * @return the user with the given ID, or null if not found.
     */
    @Override
    public User getUser(Long id) {
        return users.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Unlocks a user's account.
     * 
     * @param user_id the ID of the user to unlock.
     * @return true if the account was successfully unlocked.
     * @throws MessagingException 
     */
    @Override
    public boolean unlockAccount(Long user_id) throws MessagingException {
        User foundUser = getUser(user_id);
        foundUser.setUnlocked(true);
        users.save(foundUser);
        emailService.sendUnlockedEmail(foundUser.getUsername());
        return true;
    }

    /**
     * Logs in a user.
     * 
     * @param user the user attempting to log in.
     * @return the logged-in user with updated secret and TOTP token, or null if the password is incorrect.
     * @throws Exception if the user is not found.
     */
    @Override
    public User loginUser(User user) throws Exception {
        User foundUser = users.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (foundUser.getFailedLoginAttempts() >= 5) {
            foundUser.setUnlocked(false);
            users.save(foundUser);
            return foundUser;
        }
        if (encoder.matches(user.getPassword(), foundUser.getPassword())) {
            foundUser.setSecret(null);
            foundUser.setTotpToken(null);
            users.save(foundUser);
            Key secretKey = totpService.generateSecret();
            System.out.println(secretKey);
            TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
            foundUser.setSecret(secretKey);
            foundUser.setTotpToken(totpCode);
            System.out.println(foundUser.getSecret());
            users.save(foundUser);
            return foundUser;
        } else {
            foundUser.setFailedLoginAttempts(foundUser.getFailedLoginAttempts() + 1);
            users.save(foundUser);
            return null; // Incorrect password
        }
    }

    /**
     * Adds a new user.
     * 
     * @param user the user to add.
     * @return the added user, or null if a user with the same username already exists.
     * @throws Exception if an error occurs during the process.
     */
    @Override
    public User addUser(User user) throws Exception {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }
        user.setPassword(encoder.encode(user.getPassword()));
        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        user.setSecret(secretKey);
        user.setTotpToken(totpCode);
        user.setActivationToken(totpCode.getCode());
        user.setProvider("normal");
        user.setUnlocked(true);
        return users.save(user);
    }

    /**
     * Logs in a user using Google authentication.
     * 
     * @param email the email of the user.
     * @param role the role of the user.
     * @return the existing or newly created user.
     */
    public User googleLogin(String email, String role) {
        User existingUser = users.findByUsername(email)
                .orElseGet(() -> {
                    if (role != null) {
                        User newUser = new User(email, encoder.encode("nopassword"), role, "google", true);
                        return users.save(newUser);
                    } else {
                        return null;
                    }
                });

        return existingUser;
    }

    /**
     * Authenticates a user using email and a code.
     * 
     * @param code the code sent to the user's email.
     * @param username the username of the user.
     * @return the authenticated user, or null if the code is invalid.
     * @throws Exception if the user is not found.
     */
    @Override
    public User EmailAuth(String code, String username) throws Exception {
        User foundUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (code.equals(foundUser.getTotpToken().getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), foundUser.getTotpToken());
            if (valid) {
                foundUser.setTotpToken(null);
                foundUser.setSecret(null);
                foundUser.setActivationToken(null);
                users.save(foundUser);
                return foundUser;
            }
        }

        return null;
    }

    /**
     * Activates a user's account using an activation token.
     * 
     * @param token the activation token.
     * @return a message indicating the result of the activation process.
     * @throws Exception if the activation token is invalid.
     */
    @Override
    public String accountActivation(String token) throws Exception {
        User foundUser = users.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation code"));
        TOTPToken totpToken = foundUser.getTotpToken();
        System.out.println("IM CALLED");
        if (totpToken != null && token.equals(totpToken.getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), totpToken);
            if (valid) {
                foundUser.setEnabled(true);
                foundUser.setTotpToken(null);
                foundUser.setSecret(null);
                foundUser.setActivationToken(null);
                users.save(foundUser);
                return "Account activated successfully.";
            } else {
                return "Invalid or expired token.";
            }
        } else {
            return "Token mismatch or token not found.";
        }
    }

    /**
     * Updates a user's password.
     * 
     * @param userId the ID of the user.
     * @param user the user with the new password.
     */
    @Override
    public void updatePassword(Long userId, User user) {
        User foundUser = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        foundUser.setPassword(encoder.encode(user.getPassword()));
        users.save(user);
    }

    /**
     * Resets a user's password using a token.
     * 
     * @param userId the ID of the user.
     * @param newPassword the new password.
     * @param token the token for password reset.
     * @return a message indicating the result of the password reset process.
     * @throws Exception if the user is not found or the token is invalid.
     */
    @Override
    public String resetPassword(Long userId, String newPassword, String token) throws Exception {
        User foundUser = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        TOTPToken totpToken = foundUser.getTotpToken();

        if (totpToken != null && token.equals(totpToken.getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), totpToken);
            if (valid) {
                foundUser.setPassword(encoder.encode(newPassword));
                foundUser.setTotpToken(null);
                foundUser.setSecret(null);
                users.save(foundUser);
                return "Password updated successfully.";
            } else {
                return "Invalid or expired token.";
            }
        } else {
            return "Token mismatch or token not found.";
        }
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param userId the ID of the user to delete.
     */
    @Override
    public void deleteUser(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        users.delete(user);
    }

    @Override
    public String getProvider(Long userId){
        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getProvider();
    }
}