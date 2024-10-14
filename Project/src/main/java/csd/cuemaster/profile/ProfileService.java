package csd.cuemaster.profile;

import java.util.List;

public interface ProfileService {
    List<Profile> getAllProfile();
    Profile getProfile(Long userId, Long profileId);
    Profile updateProfile(Long userId, Profile newProfileInfo);
    Profile addProfile(Long userId, Profile profile);
    // String addProfilePhoto(Long userID, byte[] image);
    List<Profile> getPlayers();
    void sort();
    Profile pointsSet(Long user_id, Integer points);
    // void updateRank(List<Profile> sortedplayers);
}