package csd.cuemaster.match;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class MatchNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public MatchNotFoundException(long id) {
        super("Match" + id + "does not exist");
    }

}