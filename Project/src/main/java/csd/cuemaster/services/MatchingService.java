package csd.cuemaster.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.profile.Profile;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;

@Service
public class MatchingService {

    private static MatchRepository matchRepository;  // Make this static

    private static int dynamicThreshold;

    // Use @Autowired in a static method to inject the repository
    @Autowired
    public void setMatchRepository(MatchRepository matchRepository) {
        MatchingService.matchRepository = matchRepository;
    }

    public static void setDynamicThreshold(int dynamicThreshold) {
        MatchingService.dynamicThreshold = dynamicThreshold;
    }
    // Constructor to initialize dynamic threshold for pairing
    // public MatchingService(int dynamicThreshold) {
    //     this.dynamicThreshold = dynamicThreshold;
    // }
    public static List<Match> createPairs(List<Profile> profiles, Tournament tournament) {
        List<Match> matches = new ArrayList<>();

        // Ensure there are enough profiles to create pairs
        if (profiles == null || profiles.size() < 2) {
            return matches; // No pairs can be created if there are fewer than 2 profiles
        }

        // Sort profiles by rating (points)
        profiles.sort(Comparator.comparing(profile -> profile.getPoints()));

        // Pair users with similar ratings within the threshold
        for (int i = 0; i < profiles.size() - 1; i++) {
            Profile profileA = profiles.get(i);
            Profile profileB = profiles.get(i + 1);

            // Ensure the players' ratings are within the dynamic threshold for pairing
            if (Math.abs(profileA.getPoints() - profileB.getPoints()) <= dynamicThreshold) {
                User userA = profileA.getUser();
                User userB = profileB.getUser();

                            // Check if a match already exists
                // List<Match> existingMatch = matchRepository.findByTournamentAndUsers(tournament, userA, userB);
                // if (existingMatch != null) {
                //     continue;  // Skip creating this match if one already exists
                // }
                
                LocalDate matchDate = LocalDate.now();  // You can customize match date
                LocalTime matchTime = LocalTime.now();  // You can customize match time

                // Create match
                Match match = new Match(tournament, userA, userB, matchDate, matchTime, profileA.getPoints(), profileB.getPoints(), tournament.getStatus());

                matchRepository.save(match);  // Persist the match so that the ID is generated

                System.out.println("new Match:" + match.getId());
                matches.add(match);
                i++;  // Skip the next profile since it's already paired
            }
        }

        return matches;
    }

    // Method to dynamically calculate the threshold based on user profiles
    public static int calculateDynamicThreshold(List<User> users) {
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

    // // Setter to update dynamicThreshold if needed
    // public void setDynamicThreshold(int dynamicThreshold) {
    //     this.dynamicThreshold = dynamicThreshold;
    // }
}
