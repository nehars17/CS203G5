package csd.cuemaster.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ProfileNotFoundException(Long id) {
        super("Profile with user ID " + id + " not found.");
    }
    
}
