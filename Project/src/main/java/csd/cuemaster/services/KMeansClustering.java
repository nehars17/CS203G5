package csd.cuemaster.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import csd.cuemaster.user.User;

public class KMeansClustering {
    
    // Method to perform K-means clustering on players
    public static List<List<User>> clusterPlayers(List<User> players, int k) {

        //checks 
        if (players == null || players.size() < 2) {
            return new ArrayList<>();
        }
    
        if (players.size() < k) {
            k = players.size(); // Adjust k if there are fewer players than clusters
        }

        // Step 1: Get player ratings
        List<Double> ratings = players.stream()
                                      .map(user -> (double) user.getProfile().getPoints())
                                      .collect(Collectors.toList());

        // Step 2: Perform K-means clustering
        KMeans kMeans = new KMeans(k); // Assuming KMeans class exists and works for clustering
        List<Integer> clusterAssignments = kMeans.cluster(ratings);

        // Step 3: Create clusters based on K-means results
        List<List<User>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        // Assign users to their respective clusters
        for (int i = 0; i < players.size(); i++) {
            int clusterIndex = clusterAssignments.get(i);
            clusters.get(clusterIndex).add(players.get(i));
        }

        return clusters;
    }
}