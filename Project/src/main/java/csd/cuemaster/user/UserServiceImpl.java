package csd.cuemaster.user;

import java.security.Key;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import csd.cuemaster.models.TOTPToken;
import csd.cuemaster.services.TOTPService;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository users;
    private BCryptPasswordEncoder encoder;

    @Autowired
    private TOTPService totpService;

    public UserServiceImpl(UserRepository users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;

    }

    public String generateActivationToken() {
        return UUID.randomUUID().toString(); // You can store this in the database with a timestamp
    }

    public String generate2FACode() {
        Random rand = new Random();
        int code = rand.nextInt(900000) + 100000;
        System.out.println(code);
        return String.valueOf(code);
    }

    @Override
    public List<User> listUsers() {
        return users.findAll();
    }

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

    @Override
    public User getUser(Long id) {

        return users.findById(id).orElse(null);
    }

    @Override
    public User loginUser(User user) throws Exception {
        User foundUser = users.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (encoder.matches(user.getPassword(), foundUser.getPassword())) {
            Key secretKey = totpService.generateSecret();
            System.out.println(secretKey);
            TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
            foundUser.setSecret(secretKey);
            foundUser.setTotpToken(totpCode);
            System.out.println(foundUser.getSecret());
            users.save(foundUser);
            return foundUser;

        } else {
            return null; // Incorrect password
        }

    }

    /**
     * Added logic to avoid adding books with the same title
     * Return null if there exists a book with the same title
     * 
     * @throws Exception
     */
    @Override
    public User addUser(User user) throws Exception {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }
        user.setPassword(encoder.encode(user.getPassword()));
        Key secretKey = totpService.generateSecret();
        System.out.println(secretKey);
        TOTPToken totpCode = totpService.generateTOTPToken(secretKey);
        user.setSecret(secretKey);
        user.setTotpToken(totpCode);
        user.setActivationToken(totpCode.getCode());
        user.setProvider("normal");
        return users.save(user);
    }

    public User googleLogin(String email, String role) {
        // Create the authority from the role string
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

    @Override
    public String accountActivation(String token) throws Exception {
        // Find the user by the activation token
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

    @Override
    public void updatePassword(Long userId, User user) {
        User foundUser = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        foundUser.setPassword(encoder.encode(user.getPassword()));
        users.save(user);
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

    @Override
    public void deleteUser(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        users.delete(user);
    }

}