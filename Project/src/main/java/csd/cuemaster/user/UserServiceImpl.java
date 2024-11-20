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

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Override
    public List<User> listUsers() {
        return users.findAll();
    }

    @Override
    public User forgotPassword(String username) throws Exception {
        User foundUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        
        generateUserSecretAndToken(foundUser);
        return foundUser;
    }

    @Override
    public User getUser(Long id) {
        return users.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public boolean unlockAccount(Long userId) throws MessagingException {
        User foundUser = getUser(userId);
        foundUser.setUnlocked(true);
        foundUser.setFailedLoginAttempts(0);
        users.save(foundUser);
        emailService.sendUnlockedEmail(foundUser.getUsername());
        return true;
    }

    @Override
    public User loginUser(User user) throws Exception {
        User foundUser = users.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (foundUser.getFailedLoginAttempts() >= 5) {
            lockUserAccount(foundUser);
            return foundUser;
        }

        if (encoder.matches(user.getPassword(), foundUser.getPassword())) {
            generateUserSecretAndToken(foundUser);
            return foundUser;
        } else {
            incrementFailedLoginAttempts(foundUser);
            return null; // Incorrect password
        }
    }

    private void lockUserAccount(User user) {
        user.setUnlocked(false);
        users.save(user);
    }

    private void resetUserSecretAndToken(User user) throws Exception {
        user.setSecret(null);
        user.setTotpToken(null);
        user.setActivationToken(null);
        users.save(user);
    }


    private void generateUserSecretAndToken(User user) throws Exception {
        Key secretKey = totpService.generateSecret();
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        user.setSecret(secretKey);
        user.setTotpToken(totpCode);
        users.save(user);
    }

    private void incrementFailedLoginAttempts(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        users.save(user);
    }

    @Override
    public User addUser(User user) throws Exception {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }
        user.setPassword(encoder.encode(user.getPassword()));
        generateUserSecretAndToken(user);
        user.setActivationToken(user.getTotpToken().getCode());
        user.setProvider("normal");
        user.setUnlocked(true);
        return users.save(user);
    }

    public User googleLogin(String email, String role) {
        return users.findByUsername(email)
                .orElseGet(() -> {
                    if (role != null) {
                        User newUser = new User(email, encoder.encode("nopassword"), role, "google", true);
                        return users.save(newUser);
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public User EmailAuth(String code, String username) throws Exception {
        User foundUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (code.equals(foundUser.getTotpToken().getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), foundUser.getTotpToken());
            if (valid) {
                resetUserSecretAndToken(foundUser);
                return foundUser;
            }
        }
        return null;
    }

    @Override
    public String accountActivation(String token) throws Exception {
        User foundUser = users.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation code"));
        TOTPToken totpToken = foundUser.getTotpToken();
        if (totpToken != null && token.equals(totpToken.getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), totpToken);
            if (valid) {
                foundUser.setEnabled(true);
                resetUserSecretAndToken(foundUser);
                return "Account activated successfully.";
            } else {
                return "Invalid or expired token.";
            }
        } else {
            return "Token mismatch or token not found.";
        }
    }

    @Override
    public void updatePassword(Long userId, User user) {
        User foundUser = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        foundUser.setPassword(encoder.encode(user.getPassword()));
        users.save(foundUser);
    }

    @Override
    public String resetPassword(Long userId, String newPassword, String token) throws Exception {
        User foundUser = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        TOTPToken totpToken = foundUser.getTotpToken();

        if (totpToken != null && token.equals(totpToken.getCode())) {
            boolean valid = totpService.validateTOTPToken(foundUser.getSecret(), totpToken);
            if (valid) {
                foundUser.setPassword(encoder.encode(newPassword));
                resetUserSecretAndToken(foundUser);
                return "Password updated successfully.";
            } else {
                return "Invalid or expired token.";
            }
        } else {
            return "Token mismatch or token not found.";
        }
    }

    @Override
    public void deleteUser(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        users.delete(user);
    }

    @Override
    public String getProvider(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getProvider();
    }
}
