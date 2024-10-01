package csd.cuemaster.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import csd.cuemaster.user.User;
import csd.cuemaster.user.User.UserRole;

@Service
public class ProfileServiceImpl implements ProfileService {

    private ProfileRepository profiles;

    public ProfileServiceImpl(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Override
    public List<Profile> getSortedPlayers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return users.stream()
                .filter(user -> user.getRole() == UserRole.PLAYER)
                .sorted(Comparator.comparingInt(user -> ((User) user).getProfile().getPoints()).reversed())
                .map(user -> getProfile(user.getProfileId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Profile> getSortedPlayersAfterPointsReset(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return users.stream()
                .filter(user -> user.getRole() == UserRole.PLAYER)
                .sorted(Comparator.comparingInt(user -> ((User) user).getProfile().getPoints()).reversed())
                .map(user -> {
                    Profile profile = getProfile(user.getProfileId());
                    profile.setPoints(1200);
                    return profile;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Profile getProfile(Long id) {
        return profiles.findById(id).orElse(null);
    }

    @Override
    public void updateRank(List<Profile> sortedplayers) {
        int currentRank = 1;
            for (Profile profile : sortedplayers) {
                profile.setRank(currentRank);
                currentRank++;
            }
    }
}