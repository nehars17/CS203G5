package csd.cuemaster.user;

import java.util.List;

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

    @Override
    public List<User> listUsers() {
        return users.findAll();
    }

    @Override
    public User getUser(Long id) {

        return users.findById(id).orElse(null);
    }

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
        user.setAuthorities("ROLE_PLAYER");
        user.setProvider("normal");
        return users.save(user);
    }

    @Override
    public User addOrganiser(User user) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return null; // User already exists
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setAuthorities("ROLE_ORGANISER");
        user.setProvider("normal");
        return users.save(user);

    }

    public String googleLogin(String email, String role) {
        User existingUser = users.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User(email, "no password", role, "google");
                    return users.save(newUser);
                });

        return existingUser.getUsername();

    }

    @Override
    public User updateUser(Long id, User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUser(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

}