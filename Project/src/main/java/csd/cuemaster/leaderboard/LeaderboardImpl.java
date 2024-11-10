package csd.cuemaster.leaderboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileService;
import csd.cuemaster.profile.ProfileServiceImpl;
import csd.cuemaster.user.User;

public class LeaderboardImpl implements LeaderboardService{
    
    @Autowired
    ProfileService profileService;


    @Override
    public List<Profile> sortProfiles() {
        List<Profile> profileList = profileService.getPlayers();
        
        if (profileList == null || profileList.isEmpty()) {
            return profileList;
        }

        profileList.sort(Comparator.comparingInt(Profile::getPoints).reversed());
        
        return profileList;
    }

    // Set all players ranks based on the sorted points.
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sortProfiles();
        Map<Long, Integer> rankMap = new HashMap<>();
        
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }
        
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        User currentUser = currentPlayer.getUser();
        Long userId = currentUser.getId();
        rankMap.put(userId, currentRank);
        
        for (int i = 1; i < sortedPlayers.size(); i++) {
            currentPlayer = sortedPlayers.get(i);
            currentUser = currentPlayer.getUser();
            userId = currentUser.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            if (i > 0 && p1.equals(p2)) {
                rankMap.put(userId, currentRank); // Same rank for players with the same points
            } else {
                currentRank = i + 1; // Increment the rank if points are different
                rankMap.put(userId, currentRank);
            }
        }
        
        return rankMap;
    }
}
