package csd.cuemaster.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProfileAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L; 

    public ProfileAlreadyExistsException(Long userID) {
        super("User ID: " + userID + " profile already exists.");
    }
}
