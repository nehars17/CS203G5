package csd.cuemaster.imageservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImageDoesNotExistException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ImageDoesNotExistException() {
        super("Profile Photo Not Found.");
    }
}