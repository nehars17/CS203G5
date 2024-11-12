package csd.cuemaster.services;

import org.springframework.stereotype.Service;

import csd.cuemaster.tournament.Tournament.Status;

@Service
public class ScoringService {
    
    // Elo calculation constants

    // private static final int BASE_RATING = 1200;
    private static final int LOW_RATING_THRESHOLD = 1400;
    private static final int MID_RATING_THRESHOLD = 2000;
    private static final int HIGH_K_FACTOR = 40;
    private static final int MID_K_FACTOR = 32;
    private static final int LOW_K_FACTOR = 24;
    private static final double RATING_DIFFERENCE_DIVISOR = 400.0;

    // Experience-based adjustment
    private static final int NEW_PLAYER_EXPERIENCE_THRESHOLD = 20;
    private static final int MID_PLAYER_EXPERIENCE_THRESHOLD = 50;
    private static final int NEW_PLAYER_ADJUSTMENT = 10;
    private static final int MID_PLAYER_ADJUSTMENT = 5;

    /**
     * Calculate the expected score for player A against player B.
     * @param scoreA current rating of player A
     * @param scoreB current rating of player B
     * @return the expected score as a probability
     */
    public static double calculateExpectedScore(int playerPoints, int opponentPoints) {
        return 1.0 / (1 + Math.pow(10, (opponentPoints - playerPoints) / RATING_DIFFERENCE_DIVISOR));
    }




    /**
     * Calculate a dynamic K-factor based on player rating, experience, and match importance.
     * @param score current rating of the player
     * @param experienceLevel number of games the player has played
     * @param matchImportance multiplier representing match significance (e.g., finals or semifinals)
     * @return calculated dynamic K-factor
     */
    public static int getDynamicKFactor(int score, int experienceLevel, double matchImportance) {
        int baseK = (score < LOW_RATING_THRESHOLD) ? HIGH_K_FACTOR : (score < MID_RATING_THRESHOLD) ? MID_K_FACTOR : LOW_K_FACTOR;

        int experienceAdjustment = (experienceLevel < NEW_PLAYER_EXPERIENCE_THRESHOLD) ? NEW_PLAYER_ADJUSTMENT :
                                   (experienceLevel < MID_PLAYER_EXPERIENCE_THRESHOLD) ? MID_PLAYER_ADJUSTMENT : 0;

        return (int) ((baseK + experienceAdjustment) * matchImportance);
    }

    public static double getMatchImportanceMultiplier(Status matchStatus) {
        switch (matchStatus) {
            case ROUND_OF_32:
                return 1.0; // least importance
            case ROUND_OF_16:
                return 1.1;
            case QUARTER_FINALS:
                return 1.2;
            case SEMI_FINAL:
                return 1.3;
            case FINAL:
                return 1.5; // highest importance
            default:
                return 1.0; // default for non-bracket matches
        }
    }
    
    public static int calculateNewRating(int playerRating, double expectedScore, int result, Status matchStatus, int experienceLevel) {
        
        double matchImportanceMultiplier = getMatchImportanceMultiplier(matchStatus);
        
        int dynamicKFactor = getDynamicKFactor(playerRating, experienceLevel, matchImportanceMultiplier);

        return (int) (playerRating + dynamicKFactor * (result - expectedScore));
    }

    // /**
    //  * Helper method to validate that the winner is in the match.
    //  * @param winnerId ID of the player declared as the winner
    //  * @param userId1 ID of the first player in the match
    //  * @param userId2 ID of the second player in the match
    //  */
    // private void validateWinner(Long winnerId, Long userId1, Long userId2) {
    //     if (!winnerId.equals(userId1) && !winnerId.equals(userId2)) {
    //         throw new IllegalArgumentException("Player " + winnerId + " is not in the match.");
    //     }
    // }

}
