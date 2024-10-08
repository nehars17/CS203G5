package csd.cuemaster.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileIdNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ProfileIdNotFoundException(Long id) {
        super("Profile ID: " + id + " not found.");
    }

}