package csd.cuemaster.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import csd.cuemaster.user.*;
import csd.cuemaster.user.User.UserRole;

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
                      .orElseThrow(()-> new ProfileIdNotFoundException(profileId));
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
            }

            return profiles.save(profile);
        }).orElseThrow(() -> new UserProfileNotFoundException(userId));
    }

    @Override
    public Profile addProfile(Long userId, Profile profile){ 
        User user = users.findById(userId)           
                        .orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(userId) + " not found."));

        if(profiles.findByUserId(userId).isPresent()){
            throw new ProfileAlreadyExistsException(userId);
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
        }
        return profiles.save(profile);
    }

    // @Override
    // public String addProfilePhoto(Long userID, byte[] image){
    //     try{
    //         St
    //     }
    // }

    @Override
    public List<Profile> getPlayers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return users.stream()
                .filter(user -> user.getRole() == UserRole.PLAYER)
                .map(user -> getProfile(user.getProfileId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Profile> getSortedPlayers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return users.stream()
                .filter(user -> user.getRole() == UserRole.PLAYER)
                .sorted(Comparator.comparingInt(user -> ((User) user).getProfile().getPoints()).reversed())
                .map(user -> getProfile(user.getProfileId()))
                .collect(Collectors.toList());
    }

    @Override
    public Profile getProfile(Long id) {
        return profiles.findById(id).orElse(null);
    }

    @Override
    public void resetPoints(List<Profile> players) {
            for (Profile profile : players) {
                profile.setPoints(1200);
            }
    }

    @Override
    public void updateRank(List<Profile> sortedplayers) {
        int currentRank = 1;
            for (Profile profile : sortedplayers) {
                profile.setRank(currentRank);
                currentRank++;
            }
    }
}
