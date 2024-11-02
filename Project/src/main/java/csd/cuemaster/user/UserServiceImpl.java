package csd.cuemaster.user;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository users;
    private BCryptPasswordEncoder encoder;

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
    public User getUser(Long id) {

        return users.findById(id).orElse(null);
    }

    @Override
    public User loginUser(User user) {
        User foundUser = users.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (encoder.matches(user.getPassword(), foundUser.getPassword())) {
            String code = generate2FACode();
            foundUser.setAuthCode(code);
            users.save(foundUser);
            return foundUser;
            
        } else {
            return null; // Incorrect password
        }

    }

    /**
     * Added logic to avoid adding books with the same title
     * Return null if there exists a book with the same title
     */
    @Override
    public User addUser(User user) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setProvider("normal");
        String token = generateActivationToken(); // Generate token
        user.setActivationToken(token);
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
    public boolean EmailAuth(String username, String code) {
        User foundUser = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println(foundUser.getAuthCode());
        if(foundUser.getAuthCode()==null){
            return true;
        }
        if (foundUser.getAuthCode().equals(code)) {
            foundUser.setAuthCode(null);
            users.save(foundUser);
            return true;
        }
        return false;

    }

    @Override
    public String accountActivation(String token) {
        // Find the user by the activation token
        User foundUser = users.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation code"));

        // Set the user as enabled and clear the activation token
        foundUser.setEnabled(true); // Activate the user
        foundUser.setActivationToken(null); // Clear token after activation

        // Save the updated user to the database
        users.save(foundUser);

        return "Account Activated";
    }

    @Override
    public User updateUser(Long id, User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUser(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        users.delete(user);
    }

}