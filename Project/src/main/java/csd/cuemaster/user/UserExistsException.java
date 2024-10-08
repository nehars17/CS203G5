package csd.cuemaster.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserExistsException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public UserExistsException(String username) {
        super("This user exists: " + username);
    }
    
}
