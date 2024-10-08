package csd.cuemaster.profile;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import csd.cuemaster.user.*;


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
            boolean isPlayer = user.getAuthorities().stream()
                                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_PLAYER"));  //getAuthorities return a Collections

            if (isOrganizer){
                if (newProfileInfo.getOrganization() == null){
                    throw new IllegalArgumentException("Organizer cannot have a null organization");
                }
                profile.setOrganization(newProfileInfo.getOrganization());
            }else if (isPlayer){
                profile.setOrganization(null);
            }
            
            return profiles.save(profile);
        }).orElseThrow(() -> new UserProfileNotFoundException(userId));
    }

    @Override
    public Profile addProfile(Long userId, Profile profile){ 
        User user = users.findById(userId)           
                        .orElseThrow(() -> new UsernameNotFoundException("User ID: " + String.valueOf(userId) + " not found."));
        
        boolean isOrganizer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));  //getAuthorities return a Collections
        boolean isPlayer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PLAYER"));

        profile.setUser(user);
        
        if (isOrganizer){
            // profile.setFirstname(newProfileInfo.getFirstname());
            // profile.setLastname(newProfileInfo.getLastname());
            // profile.setBirthdate(newProfileInfo.getBirthdate());
            // profile.setBirthlocation(newProfileInfo.getBirthlocation());
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
}
