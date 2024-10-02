package csd.cuemaster.profile;

import java.util.List;

import csd.cuemaster.user.User;

public interface ProfileService {
    List<Profile> getPlayers(List<User> users);
    List<Profile> getSortedPlayers(List<User> users);
    Profile getProfile(Long id);
    void resetPoints(List<Profile> players);
    void updateRank(List<Profile> sortedplayers);
}