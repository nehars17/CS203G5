package csd.cuemaster.profile;

import java.util.List;

public interface ProfileService {

    List<Profile> getAllProfile();
    Profile getProfile(Long userId, Long profileId);
    Profile updateProfile(Long userId, Profile newProfileInfo);
    Profile addProfile(Long userId, Profile profile);
    // String addProfilePhoto(Long userID, byte[] image);
    List<Profile> getPlayers(List<Profile> profiles);
    void sort(List<Profile> players);
    void resetPoints(List<Profile> players);
    // void updateRank(List<Profile> sortedplayers);
}