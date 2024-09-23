package csd.cuemaster.profile;

public interface ProfileService {
    Profile getProfile(Long id);
    Profile addProfile(Profile profile);
    Profile updatProfile(Long id, Profile profile);

    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    void deleteProfile(Long id);                //check if we really need this 
}