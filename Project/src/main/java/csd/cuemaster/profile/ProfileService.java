package csd.cuemaster.profile;

import java.util.List;
import java.util.Map;

import csd.cuemaster.tournament.Tournament.Status;
import csd.cuemaster.user.User;

public interface ProfileService {
    List<Profile> getAllProfile();
    Profile getProfile(Long userId, Long profileId);
    Profile updateProfile(Long userId, Profile newProfileInfo);
    Profile addProfile(User user, Profile profile);
    
    // String addProfilePhoto(Long userID, byte[] image);
    
    List<Profile> getPlayers();
    List<Profile> sortProfiles();
    
    Map<Long, Integer> setRank();
    
    Profile pointsSet(Long user_id, Integer points);
    
    List<Profile> getProfilesFromMatches(Long match_id);
    
    void updatePlayerStatistics(Profile winnerProfile, Profile loserProfile, Long tournamentId, Status matchStatus);
    
    List<Profile> getProfilesFromTournaments(Long tournament_id);
}