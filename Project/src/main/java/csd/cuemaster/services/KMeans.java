package csd.cuemaster.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class KMeans {
    private int k;
    private int maxIterations = 100;
    
    public KMeans(int k) {
        this.k = k;
    }

    // Method to perform the K-means clustering on ratings
    public List<Integer> cluster(List<Double> ratings) {
        List<Double> centroids = initializeCentroids(ratings);
        List<Integer> assignments = new ArrayList<>(Collections.nCopies(ratings.size(), -1));

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Step 2: Assign each user to the closest centroid
            for (int i = 0; i < ratings.size(); i++) {
                double rating = ratings.get(i);
                int closestCentroid = getClosestCentroid(rating, centroids);
                assignments.set(i, closestCentroid);
            }

            // Step 3: Update centroids
            List<Double> newCentroids = updateCentroids(ratings, assignments);
            
            // If centroids do not change, break the loop
            if (newCentroids.equals(centroids)) {
                break;
            }
            centroids = newCentroids;
        }
        
        return assignments;
    }

    // Randomly initialize centroids
    private List<Double> initializeCentroids(List<Double> ratings) {
        Random rand = new Random();
        Set<Integer> selectedIndices = new HashSet<>();
        List<Double> centroids = new ArrayList<>();

        while (centroids.size() < k) {
            int index = rand.nextInt(ratings.size());
            if (!selectedIndices.contains(index)) {
                centroids.add(ratings.get(index));
                selectedIndices.add(index);
            }
        }
        return centroids;
    }

    // Find the closest centroid to a given rating
    private int getClosestCentroid(double rating, List<Double> centroids) {
        double minDistance = Double.MAX_VALUE;
        int closestCentroid = -1;
        
        for (int i = 0; i < centroids.size(); i++) {
            double distance = Math.abs(rating - centroids.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }

    // Update centroids by averaging the ratings in each cluster
    private List<Double> updateCentroids(List<Double> ratings, List<Integer> assignments) {
        List<Double> newCentroids = new ArrayList<>(Collections.nCopies(k, 0.0));
        List<Integer> counts = new ArrayList<>(Collections.nCopies(k, 0));

        for (int i = 0; i < ratings.size(); i++) {
            int clusterId = assignments.get(i);
            newCentroids.set(clusterId, newCentroids.get(clusterId) + ratings.get(i));
            counts.set(clusterId, counts.get(clusterId) + 1);
        }

        for (int i = 0; i < k; i++) {
            if (counts.get(i) > 0) {
                newCentroids.set(i, newCentroids.get(i) / counts.get(i));
            } else {
                // Handle empty clusters: reinitialize the centroid by picking a random rating
                newCentroids.set(i, ratings.get(new Random().nextInt(ratings.size())));
            }
        }
        return newCentroids;
    }

}
