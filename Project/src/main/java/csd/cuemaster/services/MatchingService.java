package csd.cuemaster.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import csd.cuemaster.match.Match;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;

@Service
public class MatchingService {

    private int dynamicThreshold;

    // Constructor to initialize dynamic threshold for pairing
    public MatchingService(int dynamicThreshold) {
        this.dynamicThreshold = dynamicThreshold;
    }

    // Method to create pairs based on user ratings within the dynamic threshold
    public List<Match> createPairs(List<User> players, Tournament tournament) {
        List<Match> matches = new ArrayList<>();

        // Sort users by rating (profile points)
        players.sort(Comparator.comparing(user -> user.getProfile().getPoints()));

        // Pair users with similar ratings within the threshold
        for (int i = 0; i < players.size() - 1; i++) {
            User userA = players.get(i);
            User userB = players.get(i + 1);

            // Ensure the players' ratings are within the dynamic threshold for pairing
            if (Math.abs(userA.getProfile().getPoints() - userB.getProfile().getPoints()) <= dynamicThreshold) {
                LocalDate matchDate = LocalDate.now(); // You can customize match date
                LocalTime matchTime = LocalTime.now(); // You can customize match time
                Match match = new Match(tournament, userA, userB, matchDate, matchTime, 0, 0, "UPCOMING");

                matches.add(match);
                i++;  // Skip the next user since they're paired
            }
        }

        return matches;
    }

    // Method to dynamically calculate the threshold based on user profiles
    public int calculateDynamicThreshold(List<User> users) {
        // Extract ratings from user profiles
        List<Integer> ratings = users.stream()
                                    .map(user -> user.getProfile().getPoints())
                                    .collect(Collectors.toList());

        if (ratings.isEmpty()) {
            return 100;  // Default threshold if no players are provided
        }

        // Calculate mean and standard deviation
        double mean = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = ratings.stream()
                                .mapToDouble(rating -> Math.pow(rating - mean, 2))
                                .average()
                                .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        // Dynamically calculate the threshold
        int dynamicThreshold = (int) Math.round(standardDeviation);

        // Adjust threshold based on number of players
        if (users.size() < 10) {
            dynamicThreshold = Math.max(dynamicThreshold, 50);  // Ensure reasonable minimum for small tournaments
        } else if (users.size() > 100) {
            dynamicThreshold = Math.min(dynamicThreshold, 200);  // Cap threshold for large tournaments
        }

        return Math.max(dynamicThreshold, 50);  // Ensure baseline threshold
    }
}
