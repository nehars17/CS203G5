package csd.cuemaster.profile;

import org.springframework.stereotype.Service;


@Service
public class ProfileServiceImpl implements ProfileService {
   
    private ProfileRepository profiles;
    

    public ProfileServiceImpl(ProfileRepository profiles){
        this.profiles = profiles;
    }

    @Override
    public Profile getProfile(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Profile addProfile(Profile profile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Profile updatProfile(Long id, Profile profile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteProfile(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}