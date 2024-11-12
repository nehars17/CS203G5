package csd.cuemaster.user;

import java.util.List;

import jakarta.mail.MessagingException;

public interface UserService {
    
    List<User> listUsers();
    User getUser(Long id);
    User addUser(User user) throws Exception;
    // User addOrganiser(User user);
    User loginUser(User user) throws Exception;
    User googleLogin(String email, String role);
    String accountActivation(String token) throws Exception;
    User EmailAuth(String code, String username) throws Exception;
    void updatePassword(Long id, User user);
    String resetPassword(Long id, String newPassword,String token) throws Exception;
    User forgotPassword(String username) throws Exception;
    boolean unlockAccount(Long user_id) throws MessagingException;
    String getProvider(Long user_id);

    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    void deleteUser(Long id);


}