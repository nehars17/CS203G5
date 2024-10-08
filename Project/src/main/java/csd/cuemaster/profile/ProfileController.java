package csd.cuemaster.profile;

import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProfileController {
    // private ProfileService profileService;
    // private ProfileRepository profilerepository;
    // private UserRepository userrepository;

    // public ProfileController(ProfileService p, ProfileRepository ps, UserRepository us){
    //     this.profileService = p;
    //     this.profilerepository = ps;
    //     this.userrepository = us;
    // }

    // @GetMapping("/{userID}/profile")
    // public Profile getProfileByProfileID(@PathVariable (value = "userId") Long userId) {

    //     // Optional<User> optionalUser = userrepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(user.getId()) + " not found."));
    //     // User user = optionalUser.get();

    //     User user = userrepository.findById(userId)             //this statement retrieves the User object from the database 
    //                      .orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(userId) + " not found.")); //This statement applies the orElseThrow() method to the Optional<User> returned by findById(userId)

    //     UserRole role = user.getRole();

    //     // if (role == UserRole.ORGANIZER){


    //     // }
    //     //testing
    //     Profile profile = profilerepository.findByUser(user)
    //             .orElseThrow(() -> new UsernameNotFoundException("Profile for User ID: " + userId + " not found."));
                
    //     return profile;
    // }

    // // Returns a sorted list of players.
    // @GetMapping("/profiles")
    // public List<Profile> getLeaderboard() {
    //     List<User> users = userrepository.findAll();
    //     List<Profile> profiles = profileService.getSortedPlayers(users);
    //     profileService.updateRank(profiles);
    //     return profiles;
    // }

    // // Returns a list of players after a points reset.
    // @PutMapping("/profiles")
    // public List<Profile> resetPoints() {
    //     List<User> users = userrepository.findAll();
    //     List<Profile> profiles = profileService.getPlayers(users);
    //     profileService.resetPoints(profiles);
    //     return profiles;
    // }
}