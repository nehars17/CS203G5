package csd.cuemaster.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;

@Service
public class ProfileServiceImpl implements ProfileService{
    
    @Autowired
    private ProfileRepository profiles;
    @Autowired
    private UserRepository users;

    @Override 
    public List<Profile> getAllProfile(){
        return profiles.findAll();
    }

    @Override 
    public Profile getProfile(Long userId, Long profileId) {
        if (!users.existsById(userId)){
            throw new UserNotFoundException(userId);
        }else if (!profiles.existsById(profileId)){
            throw new ProfileIdNotFoundException(profileId);
        }
        return profiles.findByUserId(userId)
                      .orElseThrow(()-> new UserProfileNotFoundException(userId));
    }

    //havent settle profile photo
    //can be used for PUT and POST method 
    @Override
    public Profile updateProfile(Long userId, Profile newProfileInfo){

        User user = users.findById(userId)       
                      .orElseThrow(() -> new UserNotFoundException(userId));

        return profiles.findByUserId(userId).map(profile -> {
            profile.setFirstname(newProfileInfo.getFirstname());
            profile.setLastname(newProfileInfo.getLastname());
            profile.setBirthdate(newProfileInfo.getBirthdate());
            profile.setBirthlocation(newProfileInfo.getBirthlocation());

            boolean isOrganizer = user.getAuthorities().stream()
                                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));  //getAuthorities return a Collections
            if (isOrganizer){
                if (newProfileInfo.getOrganization() == null || newProfileInfo.getOrganization().isEmpty()){
                    throw new OrganizationCannotBeNullException();
                }
                profile.setOrganization(newProfileInfo.getOrganization());
            }else {
                profile.setOrganization(null);
            }

            return profiles.save(profile);
        }).orElse(null);
    }

    @Override
    public Profile addProfile(User user, Profile profile){ 
        // User user = users.findById(userId)           
        //                 .orElseThrow(() -> new UserNotFoundException(userId));

        // profiles.findByUserId(userId).ifPresent(existingProfile ->{
        //                     throw new ProfileAlreadyExistsException(userId);});
        
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

    // @Override
    // public String addProfilePhoto(Long userID, byte[] image){
    //     try{
    //         St
    //     }
    // } getAllProfile()

    // Returns a list of all players.
    @Override
    public List<Profile> getPlayers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return new ArrayList<Profile>();
        }
        return profileList.stream()
                .filter(profile -> {
                    User user = profile.getUser();
                    return user != null && user.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"));
                })
                .collect(Collectors.toList());
    }

    // Sorts all players based on points.
    @Override
    public List<Profile> sort() {
        List<Profile> profileList = getPlayers();
        profileList.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
        return profileList;
    }

    // Sets a player's points.
    @Override
    public Profile pointsSet(Long user_id, Integer points) {
        User user = users.findById(user_id)
                .orElseThrow(() -> new UserNotFoundException(user_id));
        Profile profile = profiles.findByUserId(user_id)
                .orElseThrow(() -> new UserProfileNotFoundException(user_id));
        user = profile.getUser();
        if (user != null && user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"))) {
            profile.setPoints(points);
        }
        return profiles.save(profile);
    }

    /* @Override
    public void updateRank(List<Profile> sortedplayers) {
        int currentRank = 1;
            for (Profile profile : sortedplayers) {
                profile.setRank(currentRank);
                currentRank++;
            }
    } */
}
