package csd.cuemaster.user;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
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
    public User addPlayer(User user) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }
        
        user.setPassword(encoder.encode(user.getPassword()));
        
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));

        user.setProvider("normal");
        
        String token = generateActivationToken(); // Generate token
        
        user.setActivationToken(token);
        return users.save(user);
    }

    @Override
    public User addOrganiser(User user) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }

        user.setPassword(encoder.encode(user.getPassword()));
       
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORGANIZER")));

        user.setProvider("normal");
        String token = generateActivationToken(); // Generate token
        user.setActivationToken(token);
        return users.save(user);

    }

    @Override
    public String googleLogin(String email, List<GrantedAuthority> role) {
        User existingUser = users.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User(email, "no password", role,"google",false);
                    return users.save(newUser);
                });

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