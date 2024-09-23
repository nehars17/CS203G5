package csd.cuemaster.profile;

import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class ProfileServiceImpl implements ProfileService {
   
    private ProfileRepository profiles;
    

    public ProfileServiceImpl(ProfileRepository profiles){
        this.profiles = profiles;
    }
}