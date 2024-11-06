package csd.cuemaster.user;

import java.util.List;

public interface UserService {
    
    List<User> listUsers();
    User getUser(Long id);
    User addUser(User user);
    // User addOrganiser(User user);
    User loginUser(User user) throws Exception;
    User googleLogin(String email, String role);
    String accountActivation(String token);
    User EmailAuth(String code, String username) throws Exception;
    void updatePassword(Long id, User user);
    String resetPassword(Long id, String newPassword,String token) throws Exception;
    User forgotPassword(String username) throws Exception;
    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    void deleteUser(Long id);


}