package csd.cuemaster.profile;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.user.*;

public interface ProfileService {
    List<Profile> getAllProfile();
    Profile getProfile(Long userId);
    Profile updateProfile(Long userId, Profile newProfileInfo, MultipartFile profilephoto);
    Profile addProfile(Long userId, MultipartFile image);
    List<Profile> getPlayers();
    List<Profile> getOrganisers();
    Profile pointsSet(Long user_id, Integer points);
    List<Profile> sortProfiles();
    Map<Long, Integer> setRank();
    String getName(long user_id);
    List<Profile> getProfilesFromMatches(Long match_id);
    double calculateExpectedScore(Long match_id, Long user_id);
    List<Profile> updatePlayerStatistics(Long match_id, Long winner_id);
    List<Profile> getProfilesFromTournaments(Long tournament_id);
}