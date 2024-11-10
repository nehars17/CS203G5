package csd.cuemaster.profile;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.imageservice.ImageService;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;

@Service
public class ProfileServiceImpl implements ProfileService{

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private ProfileRepository profiles;
    @Autowired
    private UserRepository users;

    @Override 
    public List<Profile> getAllProfile(){
        return profiles.findAll();
    }

    @Override 
    public Profile getProfile(Long userId) {
        if (!users.existsById(userId)){
            throw new UserNotFoundException(userId);
        }
        return profiles.findByUserId(userId)
                      .orElseThrow(()-> new UserProfileNotFoundException(userId));
    }

    //havent settle profile photo
    //can be used for PUT and POST method 
    @Override
    public Profile updateProfile(Long userId, Profile newProfileInfo, MultipartFile profilephoto){

        User user = users.findById(userId)       
                      .orElseThrow(() -> new UserNotFoundException(userId));

        return profiles.findByUserId(userId).map(profile -> {
            profile.setFirstname(newProfileInfo.getFirstname());
            profile.setLastname(newProfileInfo.getLastname());
            profile.setBirthdate(newProfileInfo.getBirthdate());
            profile.setBirthlocation(newProfileInfo.getBirthlocation());

            if(profilephoto != null && !profilephoto.isEmpty()){

                String photoPath = imageService.saveImage(user.getId(), profilephoto);
                profile.setProfilephotopath(photoPath);
            }

            boolean isOrganizer = user.getAuthorities().stream()
                                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANISER"));  //getAuthorities return a Collections
            if (isOrganizer){
                if (newProfileInfo.getOrganization() == null || newProfileInfo.getOrganization().isEmpty()){
                    throw new OrganizationCannotBeNullException();
                }
                profile.setOrganization(newProfileInfo.getOrganization());
            }else {
                profile.setOrganization(null);
                profile.setMatchCount(newProfileInfo.getMatchCount());
                profile.setMatchWinCount(newProfileInfo.getMatchWinCount());
                profile.setTournamentCount(newProfileInfo.getTournamentCount());
                profile.setTournamentWinCount(newProfileInfo.getTournamentWinCount());
            }

            return profiles.save(profile);
        }).orElse(null);
    }

    @Override
    public Profile addProfile(User user, Profile profile, MultipartFile profilephoto){ 
        // User user = users.findById(userId)           
        //                 .orElseThrow(() -> new UserNotFoundException(userId));

        // profiles.findByUserId(userId).ifPresent(existingProfile ->{
        //                     throw new ProfileAlreadyExistsException(userId);});

        if(profilephoto != null && !profilephoto.isEmpty()){

            String photoPath = imageService.saveImage(user.getId(), profilephoto);
            profile.setProfilephotopath(photoPath);
        }else {
            throw new ProfilePhotoRequiredException();
        }
        
        boolean isOrganizer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));  //getAuthorities return a Collections
        boolean isPlayer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PLAYER"));

        profile.setUser(user);
        
        if (isOrganizer){
            profile.setPoints(null);
            profile.setMatchCount(null);
            profile.setMatchWinCount(null);
            profile.setTournamentCount(null);
            profile.setTournamentWinCount(null);
        }else if (isPlayer){
            profile.setPoints(1200);
            profile.setMatchCount(0);
            profile.setMatchWinCount(0);
            profile.setTournamentCount(0);
            profile.setTournamentWinCount(0);
            profile.setOrganization(null);
        }
        return profiles.save(profile);
    }






 // Set all players ranks based on the sorted points.
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sortProfiles();
        Map<Long, Integer> rankMap = new HashMap<>();
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        User currentUser = currentPlayer.getUser();
        Long userId = currentUser.getId();
        rankMap.put(userId, currentRank);
        for (int i = 1; i < sortedPlayers.size(); i++){
            currentPlayer = sortedPlayers.get(i);
            currentUser = currentPlayer.getUser();
            userId = currentUser.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            if (i > 0 && p1.equals(p2)) {
                rankMap.put(userId, currentRank);
            } else {
                currentRank = i + 1;
                rankMap.put(userId, currentRank);
            }
        }
        return rankMap;
    }

    // Returns a list of all players.
    @Override
    public List<Profile> getPlayers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return profileList;
        }
        return profileList.stream()
                .filter(profile -> {
                    User user = profile.getUser();
                    return user != null && user.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"));
                })
                .collect(Collectors.toList());
    }

    // Sort all players based on points.
    @Override
    public List<Profile> sortProfiles() {
        List<Profile> profileList = getPlayers();
        sort(profileList);
        return profileList;
    }

    // Helper method to sort points based on points.
    private void sort(List<Profile> players) {
        players.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
    }


    
    @Override
    public List<Profile> getOrganisers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return new ArrayList<>();
        }
        return profileList.stream()
        .filter(profile -> {
            User user = profile.getUser();
            return user != null && user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ORGANISER"));
        })
                .collect(Collectors.toList());
    }

    // Sets a player's points.
    @Override
    public Profile pointsSet(Long user_id, Integer points) {
        User user = users.findById(user_id)           
                         .orElseThrow(() -> new UserNotFoundException(user_id));
        Profile profile = profiles.findByUserId(user_id)
                .orElseThrow(() -> new UserProfileNotFoundException(user_id));
        if (user != null && user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"))) {
            profile.setPoints(points);
        }
        return profiles.save(profile);
    }

    @Override
    public String getName(long user_id){
        User user = users.findById(user_id)           
                        .orElseThrow(() -> new UserNotFoundException(user_id));

        // Retrieve the profile using the user_id
        Profile profile = profiles.findByUserId(user_id)
                                .orElseThrow(() -> new ProfileIdNotFoundException(user_id));    // Handle case where profile is not found

        String fullname = profile.getFirstname() + " " + profile.getLastname();
        return fullname;
    }




    public void increaseTournamentCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int tournamentcount = profile.getTournamentCount() + 1;
        profile.setTournamentCount(tournamentcount);
        profiles.save(profile);
    }

    public void TournamentWinCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int tournamentWincount = profile.getTournamentWinCount() + 1;
        profile.setTournamentWinCount(tournamentWincount);
        profiles.save(profile);
    }

    public void increaseMatchCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int matchcount = profile.getMatchCount() + 1;
        profile.setMatchCount(matchcount);
        profiles.save(profile);
    }

    public void MatchWinCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int matchWincount = profile.getMatchWinCount() + 1;
        profile.setTournamentWinCount(matchWincount);
        profiles.save(profile);
    }

}
