package csd.cuemaster.profile;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    
    // @PostMapping("users/{user_id}/profile/profilephoto")
    // public String postMethodName(@PathVariable (value = "user_id") Long user_id,  @RequestBody byte[] imageData) {
    //     return profileService.addProfilePhoto(user_id,imageData);
    // }

    // Returns a sorted list of players.
    @GetMapping("/profiles")
    public List<Profile> getLeaderboard() {
        List<Profile> profiles = profileService.getAllProfile();
        profiles = profileService.getPlayers(profiles);
        profileService.sort(profiles);
        // profileService.updateRank(profiles);
        return profiles;
    }

    // Returns a list of players after a points reset.
    @PutMapping("/profiles")
    public List<Profile> getLeaderboardAfterPointsReset() {
        List<Profile> profiles = profileService.getAllProfile();
        profiles = profileService.getPlayers(profiles);
        profileService.sort(profiles);
        profileService.resetPoints(profiles);
        return profiles;
    }
}