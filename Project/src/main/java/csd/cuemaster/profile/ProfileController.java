package csd.cuemaster.profile;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.imageservice.ImageService;
import csd.cuemaster.user.UserRepository;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;

@RestController
@MultipartConfig
public class ProfileController {
    private ProfileService profileService;
    private UserRepository users;
    private ProfileRepository profiles;

    public ProfileController(ProfileService profileService, UserRepository users, ProfileRepository profiles) {
        this.profileService = profileService;
        this.users = users;
        this.profiles = profiles;
    }

    @GetMapping("/profiles")
    public List<Profile> getAllProfiles(@RequestParam(required = false) String role) {
        if (role != null && role.equals("Player")) {
            System.out.println("player");
            return profileService.getPlayers();
        } else if (role != null && role.equals("Organizer")) {
            System.out.println("organizer");
            return profileService.getOrganisers();
        }
        return profileService.getAllProfile();
    }

    @GetMapping("/profile/{user_id}")
    public Profile getUserProfile(@PathVariable(value = "user_id") Long user_id) {
        return profileService.getProfile(user_id);
    }

    @GetMapping("/userName/{user_id}")
    public String getUserName(@PathVariable(value = "user_id") Long user_id){
        return profileService.getName(user_id);
    }

    @PutMapping("/user/{user_id}/profile/edit")
    public Profile putExistingProfile(@PathVariable(value = "user_id") Long user_id,
                                    @RequestPart("profile") @Valid Profile newProfileInfo, 
                                    @RequestPart("profilePhoto") MultipartFile newprofilePhoto) {

        return profileService.updateProfile(user_id, newProfileInfo, newprofilePhoto);
    }

    @PostMapping("create/profile/{user_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Profile postProfile(@PathVariable(value = "user_id") Long user_id,
                               @RequestPart("profile") @Valid Profile profile,
                               @RequestPart("profilePhoto") MultipartFile profilePhoto) {
        return profileService.addProfile(user_id, profilePhoto);
    }

    // Return a sorted list of players.
    @GetMapping("/leaderboard")
    public List<Profile> getLeaderboard() {
        List<Profile> sortedProfileList = profileService.sortProfiles();
        return sortedProfileList;
    }

    // Return a sorted list of players.
    @GetMapping("/playerrank")
    public Map<Long, Integer> getPlayerRank() {
        return profileService.setRank();
    }

    // Change a player's points.
    @PutMapping("/changepoints/{userId}")
    public Profile changePoints(@PathVariable (value = "userId") Long userId, @RequestBody Profile profile) {
        Integer newpoints = profile.getPoints();
        return profileService.pointsSet(userId, newpoints);
    }

    // Change a player's stats.
    @PutMapping("/playerstats/{matchId}/{winnerId}")
    public List<Profile> changePlayerStats(@PathVariable (value = "matchId") Long matchId,
            @PathVariable (value = "winnerId") Long winnerId) {
        return profileService.updatePlayerStatistics(matchId, winnerId);
    }
}