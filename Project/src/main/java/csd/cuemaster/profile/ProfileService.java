package csd.cuemaster.profile;

import java.util.List;

import csd.cuemaster.user.User;

public interface ProfileService {
    List<Profile> getSortedPlayers(List<User> users);
    List<Profile> getSortedPlayersAfterPointsReset(List<User> users);
    Profile getProfile(Long id);
    void updateRank(List<Profile> sortedplayers);
}