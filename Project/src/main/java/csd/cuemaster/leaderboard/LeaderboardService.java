package csd.cuemaster.leaderboard;

import java.util.List;
import java.util.Map;

import csd.cuemaster.profile.Profile;

public interface LeaderboardService {
    
    List<Profile> sortProfiles();

    Map<Long, Integer> setRank();
}
