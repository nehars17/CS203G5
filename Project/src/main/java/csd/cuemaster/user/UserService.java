package csd.cuemaster.user;

import java.util.List;

public interface UserService {
    
    List<User> listUsers();
    User getUser(Long id);
    User addPlayer(User user);
    User addOrganiser(User user);
    User updateUser(Long id, User user);
    public User loginUser(User user);
    public String googleLogin(String email, String role);
    public String accountActivation(String token);

    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    void deleteUser(Long id);


}