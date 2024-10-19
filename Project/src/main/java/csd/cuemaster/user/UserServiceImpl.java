package csd.cuemaster.user;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    // @Override
    // public User addOrganiser(User user) {
    //     if (users.findByUsername(user.getUsername()).isPresent()) {
    //         return null; // User already exists
    //     }

    //     user.setPassword(encoder.encode(user.getPassword()));
    //     user.setAuthorities("ROLE_ORGANISER");
    //     user.setProvider("normal");
    //     String token = generateActivationToken(); // Generate token
    //     user.setActivationToken(token);
    //     return users.save(user);

    // }

    public String googleLogin(String email, String role) {
        // Create the authority from the role string
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
    
        User existingUser = users.findByUsername(email)
            .orElseGet(() -> {
                List<String> roles = Collections.singletonList(role); // Assuming roles are stored as strings
                User newUser = new User(email, encoder.encode("nopassword"), roles, "google", true);
                return users.save(newUser);
            });
    
        // Convert the user's roles to SimpleGrantedAuthority
        List<SimpleGrantedAuthority> authorities = existingUser.getRoles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    
        // Now you can use the authorities as needed
        return existingUser.getUsername();
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