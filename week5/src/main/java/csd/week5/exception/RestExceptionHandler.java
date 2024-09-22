package csd.week5.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralize exception handling in this class.
 */

 // this will basically override the system other extra stuff of returning errors, and just go through whatever you set 
 //like only the meesage field 
@ControllerAdvice
public class RestExceptionHandler{

    /**
     * TODO - Activity 1
     * Study the below code to understand consistent exception handling
     * 
     */
    

    
    /**
     * Construct a new ResponseEntity to customize the Http error messages
     * This method handles the case in which validation failed for
     * controller method's arguments.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the case in which arguments for controller's methods did not match the type.
     * E.g., bookId passed in is not a number
     */
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}