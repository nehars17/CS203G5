package csd.cuemaster.profile;

import java.util.List;

public interface ProfileService {

    List<Profile> getAllProfile();
    Profile getProfile(Long id);
    Profile updateProfile(Long userId, Profile newProfileInfo);
    Profile addProfile(Long userId, Profile profile);
}
