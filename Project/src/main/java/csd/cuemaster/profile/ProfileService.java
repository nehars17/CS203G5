package csd.cuemaster.profile;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    List<Profile> getAllProfile();
    Profile getProfile(Long userId);
    Profile updateProfile(Long userId, Profile newProfileInfo, MultipartFile profilephoto);
    Profile addProfile(Long userId, Profile profile, MultipartFile image);
    List<Profile> getPlayers();
    List<Profile> getOrganisers();
    List<Profile> sortProfiles();
    Map<Long, Integer> setRank();
    String getName(long userId);
    List<Profile> getProfilesFromMatches(Long matchId);
    double calculateExpectedScore(Long matchId, Long userId);
    List<Profile> updatePlayerStatistics(Long matchId, Long winnerId);
    List<Profile> getProfilesFromTournaments(Long tournamentId);
    void increaseTournamentCount(Long userId);
    void decreaseTournamentCount(Long userId);
    void TournamentWinCount(Long userId);
}