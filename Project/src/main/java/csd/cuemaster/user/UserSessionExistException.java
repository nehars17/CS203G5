package csd.cuemaster.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserSessionExistException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserSessionExistException(String message) {
        super(message);
    }
}
