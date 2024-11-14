package csd.cuemaster.profile;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;

/**
 * Controller class for managing profile-related endpoints in the application.
 */
@RestController
@MultipartConfig
public class ProfileController {
    private ProfileService profileService;

    /**
     * Constructs a ProfileController with the specified ProfileService.
     *
     * @param profileService the service layer for profile operations
     */
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Retrieves all profiles or filters by role if specified.
     *
     * @param role the role to filter profiles by (e.g., "Player" or "Organizer")
     * @return a list of profiles based on the specified role or all profiles if no role is specified
     */
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

    /**
     * Retrieves a profile by user ID.
     *
     * @param userId the ID of the user
     * @return the profile of the specified user
     */
    @GetMapping("/profile/{userId}")
    public Profile getUserProfile(@PathVariable(value = "userId") Long userId) {
        return profileService.getProfile(userId);
    }

    /**
     * Retrieves the name of a user by user ID.
     *
     * @param userId the ID of the user
     * @return the name of the specified user
     */
    @GetMapping("/fullname/{userId}")
    public String getUserName(@PathVariable(value = "userId") Long userId) {
        return profileService.getName(userId);
    }

    /**
     * Updates an existing profile with new information and profile photo.
     *
     * @param userId         the ID of the user
     * @param newProfileInfo  the new profile information to update
     * @param newprofilePhoto the new profile photo file
     * @return the updated profile
     */
    @PutMapping("/user/{userId}/profile")
    public Profile putExistingProfile(@PathVariable(value = "userId") Long userId,
                                      @RequestPart("profile") @Valid Profile newProfileInfo,
                                      @RequestPart("profilePhoto") MultipartFile newprofilePhoto) {
        return profileService.updateProfile(userId, newProfileInfo, newprofilePhoto);
    }

    /**
     * Creates a new profile with profile information and profile photo.
     *
     * @param userId     the ID of the user
     * @param profile     the profile information to create
     * @param profilePhoto the profile photo file to upload
     * @return the newly created profile
     */
    @PostMapping("/user/{userId}/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public Profile postProfile(@PathVariable(value = "userId") Long userId,
                               @RequestPart("profile") @Valid Profile profile,
                               @RequestPart("profilePhoto") MultipartFile profilePhoto) {
        return profileService.addProfile(userId, profile, profilePhoto);
    }

    /**
     * Retrieves a sorted leaderboard of players based on their statistics.
     *
     * @return a sorted list of player profiles for the leaderboard
     */
    @GetMapping("/leaderboard")
    public List<Profile> getLeaderboard() {
        List<Profile> sortedProfileList = profileService.sortProfiles();
        return sortedProfileList;
    }

    /**
     * Retrieves a map of player ranks.
     *
     * @return a map of user IDs and their respective ranks
     */
    @GetMapping("/playerrank")
    public Map<Long, Integer> getPlayerRank() {
        return profileService.setRank();
    }

    /**
     * Updates player statistics after a match based on match ID and winner ID.
     *
     * @param matchId  the ID of the match
     * @param winnerId the ID of the winner
     * @return a list of profiles with updated player statistics
     */
    @PutMapping("/playerstats/{matchId}/{winnerId}")
    public List<Profile> changePlayerStats(@PathVariable(value = "matchId") Long matchId,
                                           @PathVariable(value = "winnerId") Long winnerId) {
        return profileService.updatePlayerStatistics(matchId, winnerId);
    }
}