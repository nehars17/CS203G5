package csd.cuemaster.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserNotAuthenticatedException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
    
}
