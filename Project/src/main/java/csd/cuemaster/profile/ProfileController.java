package csd.cuemaster.profile;

import java.util.Optional;
import java.util.List;

import jakarta.validation.Valid;


import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class ProfileController {
    private ProfileService profileService; 

    public ProfileController (ProfileService profileService){
        this.profileService = profileService;
    }

    @GetMapping("/profiles")
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfile();
    }

    @GetMapping("/users/{user_id}/profile/{profile_id}")
    public Profile getUserProfile(@PathVariable (value = "user_id") Long user_id,@PathVariable Long profile_id) {
        return profileService.getProfile(user_id,profile_id);
    }
    
    @PutMapping("/user/{user_id}/profile/edit")
    public Profile putExistingProfile(@PathVariable Long user_id, @Valid @RequestBody Profile newProfileInfo) {
        return profileService.updateProfile(user_id, newProfileInfo);
    }

    @PostMapping("users/{user_id}/profile")
    public Profile postMethodName(@PathVariable (value = "user_id") Long user_id, @Valid @RequestBody Profile profile) {
        return profileService.addProfile(user_id, profile);
    }
    
}