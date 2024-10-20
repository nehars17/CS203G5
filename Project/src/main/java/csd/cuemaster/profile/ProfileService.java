package csd.cuemaster.profile;

import java.util.List;
import csd.cuemaster.user.*;

public interface ProfileService {
    List<Profile> getAllProfile();
    Profile getProfile(Long userId, Long profileId);
    Profile updateProfile(Long userId, Profile newProfileInfo);
    Profile addProfile(User user, Profile profile);
    // String addProfilePhoto(Long userID, byte[] image);
    List<Profile> getPlayers();
    List<Profile> sort();
    Profile pointsSet(Long user_id, Integer points);
    // void updateRank(List<Profile> sortedplayers);
}