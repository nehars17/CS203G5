package csd.cuemaster.profile;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.match.MatchNotFoundException;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;

@RestController
public class ProfileController {
    private ProfileService profileService; 
    private UserRepository users;
    private ProfileRepository profiles; 

    public ProfileController (ProfileService profileService, UserRepository users, ProfileRepository profiles){
        this.profileService = profileService;
        this.users = users;
        this.profiles = profiles; 
    }

    @GetMapping("/profiles")
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfile();
    }

    @GetMapping("/user/{user_id}/profile/{profile_id}")
    public Profile getUserProfile(@PathVariable (value = "user_id") Long user_id,@PathVariable Long profile_id) {
        return profileService.getProfile(user_id,profile_id);
    }
    
    @PutMapping("/user/{user_id}/profile/edit")
    public Profile putExistingProfile(@PathVariable (value = "user_id") Long user_id, @Valid @RequestBody Profile newProfileInfo) { 
        return profileService.updateProfile(user_id, newProfileInfo);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("user/{user_id}/profile")
    public Profile postProfile(@PathVariable (value = "user_id") Long user_id, @Valid @RequestBody Profile profile){

        User user = users.findById(user_id).orElseThrow(() -> new UserNotFoundException(user_id));

        if(profiles.findByUserId(user_id).isPresent()){
            
            throw new ProfileAlreadyExistsException(user_id);
        }

        return profileService.addProfile(user, profile);
    }

    // @PostMapping("users/{user_id}/profile/profilephoto")
    // public String postMethodName(@PathVariable (value = "user_id") Long user_id,  @RequestBody byte[] imageData) {
    //     return profileService.addProfilePhoto(user_id,imageData);
    // }

    // Return a sorted list of players.
    @GetMapping("/leaderboard")
    public List<Profile> getLeaderboard() {
        List<Profile> sortedProfileList = profileService.sortProfiles();
        return sortedProfileList;
    }

    /**
     * Update a player's points.
     *
     * @param userId the ID of the user whose points are to be updated
     * @param profile containing the new points
     * @return Updated profile with the new points
     */
    @PutMapping("/changepoints/{userId}")
    public Profile changePoints(@PathVariable(value = "userId") Long userId, @RequestBody Profile profile) {
        Integer newPoints = profile.getPoints();
        return profileService.pointsSet(userId, newPoints);
    }

    /**
     * Update player statistics based on match results.
     *
     * @param matchId  the ID of the match
     * @param winnerId the ID of the winning user
     * @return List of updated profiles (winner and loser)
     */
    @PutMapping("/playerstats/{matchId}/{winnerId}")
    public ResponseEntity<List<Profile>> changePlayerStats(
            @PathVariable(value = "matchId") Long matchId,
            @PathVariable(value = "winnerId") Long winnerId) {
        try {
            Profile winnerProfile = profileService.getProfile(winnerId, winnerId); // Get winner's profile
            Profile loserProfile = profileService.getProfilesFromMatches(matchId).stream()
                    .filter(profile -> !profile.getUser().getId().equals(winnerId))
                    .findFirst()
                    .orElseThrow(() -> new MatchNotFoundException(matchId));

            profileService.updatePlayerStatistics(winnerProfile, loserProfile, matchId, Tournament.Status.ONGOING); // Assuming Status is ONGOING
            return ResponseEntity.ok(List.of(winnerProfile, loserProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}