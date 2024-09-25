package csd.cuemaster.profile;

import java.util.Optional;

import jakarta.validation.Valid;


import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import csd.cuemaster.user.UserRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.User.UserRole;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class ProfileController {
    private ProfileRepository profilerepository;
    private UserRepository userrepository;

    public ProfileController(ProfileRepository ps, UserRepository us){
        this.profilerepository = ps;
        this.userrepository = us;
    }

    @GetMapping("/{userID}/profile")
    public Profile getProfileByProfileID(@PathVariable (value = "userId") Long userId) {

        // Optional<User> optionalUser = userrepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(user.getId()) + " not found."));
        // User user = optionalUser.get();

        User user = userrepository.findById(userId)             //this statement retrieves the User object from the database 
                         .orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(userId) + " not found.")); //This statement applies the orElseThrow() method to the Optional<User> returned by findById(userId)

        UserRole role = user.getRole();

        if (role == UserRole.ORGANIZER){


        }
    }
    
}