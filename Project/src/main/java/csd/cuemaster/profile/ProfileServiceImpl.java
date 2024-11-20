package csd.cuemaster.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.imageservice.ImageService;
import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchNotFoundException;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentNotFoundException;
import csd.cuemaster.tournament.TournamentRepository;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;

@Service
public class ProfileServiceImpl implements ProfileService{

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private ProfileRepository profiles;
    @Autowired
    private UserRepository users;
    @Autowired
    private MatchRepository matches;
    @Autowired
    private TournamentRepository tournaments;

    /**
     * Return all profiles.
     * 
     * @return a list of all users.
     */
    @Override
    public List<Profile> getAllProfile(){
        return profiles.findAll();
    }

    /**
     * Return the profile of the requested userId.
     * 
     * @param userId the requested userId.
     * @return a profile.
     * @throws UserNotFoundException if user is not found.
     * @throws UserProfileNotFoundException if profile is not found for the user.
     */
    @Override 
    public Profile getProfile(Long userId) {
        checkIfUserExists(userId);
        return profiles.findByUserId(userId)
                      .orElseThrow(()-> new UserProfileNotFoundException(userId));
    }

    /**
     * Return the updated profile of the requested userId.
     * 
     * @param userId the requested userId.
     * @param newProfileInfo new profile details to be updated.
     * @param profilephoto a profile photo to be updated.
     * @return a profile with the new profile details.
     * @throws UserNotFoundException if user is not found.
     * @throws UserProfileNotFoundException if profile is not found for the user.
     */
    @Override
    public Profile updateProfile(Long userId, Profile newProfileInfo, MultipartFile profilephoto){
        User user = getUser(userId);
        return profiles.findByUserId(userId).map(profile -> {

            //Set the updated profile mandatory details 
            profile.setFirstname(newProfileInfo.getFirstname());
            profile.setLastname(newProfileInfo.getLastname());
            profile.setBirthdate(newProfileInfo.getBirthdate());
            profile.setBirthlocation(newProfileInfo.getBirthlocation());

            //If a new profile photo is provided, replace the existing one.
            if (profilephoto != null && !profilephoto.isEmpty()){
                createProfilePhoto(profilephoto, user, profile);
            }

            //Check if user is an organiser 
            boolean isOrganiser = getIsOrganiser(user);

            // Update profile details.
            updateDetails(newProfileInfo, profile, isOrganiser);

            //Save the profile and return the profile
            return profiles.save(profile);
        }).orElse(null);
    }

    /**
     * Add a new profile to a user along with a profile photo.
     * 
     * @param userId the requested userId.
     * @param profile the profile to be added.
     * @param profilephoto a profile photo to be added.
     * @return a profile associated with the user.
     * @throws UserNotFoundException if user is not found.
     * @throws UserProfileNotFoundException if profile is not found for the user.
     * @throws ProfileIdNotFoundException if profile is not found.
     * @throws ProfilePhotoRequiredException() if no profile photo is given.
     */
    @Override
    public Profile addProfile(Long userId, Profile profile, MultipartFile profilephoto) {
        // Get the User Object
        User user = getUser(userId);
        //Check if the user has a profile, throw exception if user already has profile 
        checkIfUserProfileExists(userId, user);
        //set the profile to the user 
        profile.setUser(user);
        //Set the profilephoto path
        createProfilePhoto(profilephoto, user, profile);
        //Check if the user is an organiser
        boolean isOrganiser = getIsOrganiser(user);
        //Check if the user is a player
        boolean isPlayer = getIsPlayer(user);
        // Create profile details.
        setDetails(profile, isOrganiser, isPlayer);
        // Save the profile and return the profile
        return profiles.save(profile);
    }

    /**
     * Return a list of all players.
     * 
     * @return a list of player profiles.
     */
    @Override
    public List<Profile> getPlayers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return profileList;
        }
        return filterByPlayers(profileList);
    }

    /**
     * Return a list of all organisers.
     * 
     * @return a list of organiser profiles.
     */
    @Override
    public List<Profile> getOrganisers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return new ArrayList<>();
        }
        return filterByOrganisers(profileList);
    }

    /**
     * Sort all players based on points.
     * 
     * @return a list of player profiles sorted by descending order of points.
     */
    @Override
    public List<Profile> sortProfiles() {
        List<Profile> profileList = getPlayers();
        sort(profileList);
        return profileList;
    }

    /**
     * Set all players ranks based on the sorted points.
     * 
     * @return a map of userIds and the corresponding rank.
     */
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sortProfiles();
        Map<Long, Integer> rankMap = new HashMap<>();
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }

        // Set the first player's rank to 1.
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        User currentUser = currentPlayer.getUser();
        Long userId = currentUser.getId();
        rankMap.put(userId, currentRank);

        // Set everyone else's rank.
        for (int i = 1; i < sortedPlayers.size(); i++) {
            currentPlayer = sortedPlayers.get(i);
            currentUser = currentPlayer.getUser();
            userId = currentUser.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            currentRank = checkTies(rankMap, currentRank, userId, i, p1, p2);
        }
        return rankMap;
    }

    /**
     * Return a full name of a given userId.
     * 
     * @param userId the requested userId.
     * @return a string containing the full name.
     * @throws UserNotFoundException if user is not found.
     */
    @Override
    public String getName(long userId){
        checkIfUserExists(userId);
        // Retrieve the profile using userId.
        Profile profile = getProfile(userId);
        String fullname = profile.getFirstname() + " " + profile.getLastname();
        return fullname;
    }

    /**
     * Increase the tournament count by one.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    public void increaseTournamentCount(Long userId){
        checkIfUserExists(userId);
        Profile profile = getProfile(userId);
        int tournamentcount = profile.getTournamentCount() + 1;
        profile.setTournamentCount(tournamentcount);
        profiles.save(profile);
    }

    /**
     * Decrease the tournament count by one.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    public void decreaseTournamentCount(Long userId){
        checkIfUserExists(userId);
        Profile profile = getProfile(userId);
        int tournamentcount = profile.getTournamentCount() - 1;
        profile.setTournamentCount(tournamentcount);
        profiles.save(profile);
    }

    /**
     * Increase the tournament win count by one.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    public void TournamentWinCount(Long userId){
        checkIfUserExists(userId);
        Profile profile = getProfile(userId);
        int tournamentWincount = profile.getTournamentWinCount() + 1;
        profile.setTournamentWinCount(tournamentWincount);
        profiles.save(profile);
    }

    /**
     * Increase the match count by one.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    public void increaseMatchCount(Long userId){
        checkIfUserExists(userId);
        Profile profile = getProfile(userId);
        int matchcount = profile.getMatchCount() + 1;
        profile.setMatchCount(matchcount);
        profiles.save(profile);
    }

    /**
     * Increase the match win count by one.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    public void MatchWinCount(Long userId){
        checkIfUserExists(userId);
        Profile profile = getProfile(userId);
        int matchWincount = profile.getMatchWinCount() + 1;
        profile.setTournamentWinCount(matchWincount);
        profiles.save(profile);
    }

    /**
     * Retrieve player profiles from a given match.
     * 
     * @param matchId the requested matchId.
     * @return a list of profiles from the match.
     * @throws MatchNotFoundException if match is not found.
     */
    @Override
    public List<Profile> getProfilesFromMatches(Long matchId) {
        Match match = matches.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
        List<Profile> retrieved = new ArrayList<>();
        addProfileIfExists(match.getUser1(), retrieved);
        addProfileIfExists(match.getUser2(), retrieved);
        return retrieved;
    }

    /**
     * Calculate the expected score of a given player in a given match.
     * 
     * @param matchId the requested matchId.
     * @param userId the requested userId.
     * @return an expected score of a plyer.
     * @throws IllegalArgumentException if player is not found.
     * @throws IllegalArgumentException if player is not in the match.
     */
    @Override
    public double calculateExpectedScore(Long matchId, Long userId) {
        User user = getUser(userId);
        List<Profile> players = getProfilesFromMatches(matchId);

        // Check if the player exists in the match.
        if (!(user.getProfile().equals(players.get(0)) || (user.getProfile().equals(players.get(1))))) {
            throw new IllegalArgumentException("Player " + user.getId() + " is not in the match.");
        }
        validatePlayersInMatch(players, matchId);
        List<Integer> points = getPointsFromProfiles(players);
        Integer pointsA = points.get(0);
        Integer pointsB = points.get(1);
        double expectedScoreA = calculateExpectedScore(pointsA, pointsB);
        double expectedScoreB = calculateExpectedScore(pointsB, pointsA);
        return getPlayerExpectedScore(user, players, expectedScoreA, expectedScoreB);
    }

    /**
     * Update player statistics after a winner is declared.
     * 
     * @param matchId the requested matchId.
     * @param winnerId the winner of a match.
     * @return a list containing the updated statistics of the players in the match.
     * @throws MatchNotFoundException if match is not found.
     * @throws IllegalArgumentException if winner is not found.
     * @throws IllegalArgumentException if player is not found.
     * @throws IllegalArgumentException if player is not in the match.
     */
    @Override
    public List<Profile> updatePlayerStatistics(Long matchId, Long winnerId) {
        Match match = matches.findById(matchId).orElseThrow(()-> new MatchNotFoundException(matchId));
        Long userId1 = match.getUser1().getId();
        Long userId2 = match.getUser2().getId();
        validateWinner(winnerId, userId1, userId2);
        List<Profile> players = getProfilesFromMatches(matchId);
        Integer originalPointsA = players.get(0).getPoints();
        Integer originalPointsB = players.get(1).getPoints();
        double expectedScoreA = calculateExpectedScore(matchId, userId1);
        double expectedScoreB = calculateExpectedScore(matchId, userId2);

        // Update player statistics based on the winner.
        if (winnerId == userId1) {
            updatePlayerStats(players.get(0), originalPointsA, expectedScoreA, true);
            updatePlayerStats(players.get(1), originalPointsB, expectedScoreB, false);
        } else if (winnerId == userId2) {
            updatePlayerStats(players.get(0), originalPointsA, expectedScoreA, false);
            updatePlayerStats(players.get(1), originalPointsB, expectedScoreB, true);
        }
        profiles.saveAll(players);
        return players;
    }

    /**
     * Retrieve player profiles from a given tournament.
     * 
     * @param tournamentId the requested tournamentId.
     * @return a list of profiles from the tournament.
     * @throws TournamentNotFoundException if tournament is not found.
     * @throws UserNotFoundException if user is not found.
     */
    @Override
    public List<Profile> getProfilesFromTournaments(Long tournamentId) {
        Tournament tournament = tournaments.findById(tournamentId).orElseThrow(()-> new TournamentNotFoundException(tournamentId));
        List<Profile> retrieved = new ArrayList<>();
        List<Long> players = tournament.getPlayers();
        for (Long player : players) {
            User user = users.findById(player).orElseThrow(()-> new UserNotFoundException(player));
            addProfileIfExists(user, retrieved);
        }
        return retrieved;
    }















    // START OF HELPER METHODS

    /**
     * Helper method to check if user exists.
     * 
     * @param userId the requested userId.
     * @throws UserNotFoundException if user is not found.
     */
    // Helper method to check if user exists.
    private void checkIfUserExists(Long userId) {
        if (!users.existsById(userId)){
            throw new UserNotFoundException(userId);
        }
    }

    /**
     * Helper method to check if user exists.
     * 
     * @param userId the requested userId.
     * @return a user from userId.
     * @throws UserNotFoundException if user is not found.
     */
    // Helper method to retrieve user.
    private User getUser(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user;
    }

    /**
     * Helper method to check if user already has a profile.
     * 
     * @param userId the requested userId.
     * @param user the requested user.
     * @throws ProfileAlreadyExistsException if user already has a profile.
     */
    private void checkIfUserProfileExists(Long userId, User user) {
        if (user.getProfile() != null) {
            throw new ProfileAlreadyExistsException(userId);
        }
    }

    /**
     * Helper method to set a new profile photo.
     * 
     * @param profilephoto a profile photo to be created.
     * @param user the requested user.
     * @param profile the requested profile.
     * @throws ProfilePhotoRequiredException() if no profile photo is given.
     */
    private void createProfilePhoto(MultipartFile profilephoto, User user, Profile profile) {
        if (profilephoto != null && !profilephoto.isEmpty()){
            String photoPath = imageService.saveImage(user.getId(), profilephoto);
            profile.setProfilephotopath(photoPath);
        } else {
            throw new ProfilePhotoRequiredException();
        }
    }

    /**
     * Helper method to check if the user is an organiser.
     * 
     * @param user the requested user.
     * @return true if the user is an organiser.
     */
    private boolean getIsOrganiser(User user) {
        boolean isOrganiser = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));
        return isOrganiser;
    }

    /**
     * Helper method to check if the user is a player.
     * 
     * @param user the requested user.
     * @return true if the user is an player.
     */
    private boolean getIsPlayer(User user) {
        boolean isPlayer = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PLAYER"));
        return isPlayer;
    }

    /**
     * Helper method to update profile details.
     * 
     * @param newProfileInfo the requested user.
     * @param profile the requested profile.
     * @param isOrganizer true if organizer.
     * @throws OrganizationCannotBeNullException() if there is no organization.
     */
    private void updateDetails(Profile newProfileInfo, Profile profile, boolean isOrganizer) {
        if (isOrganizer){
            if (newProfileInfo.getOrganization() == null || newProfileInfo.getOrganization().isEmpty()){
                throw new OrganizationCannotBeNullException();
            }
            profile.setOrganization(newProfileInfo.getOrganization());
        } else {
            profile.setPoints(newProfileInfo.getPoints());
            profile.setOrganization(null);
            profile.setMatchCount(newProfileInfo.getMatchCount());
            profile.setMatchWinCount(newProfileInfo.getMatchWinCount());
            profile.setTournamentCount(newProfileInfo.getTournamentCount());
            profile.setTournamentWinCount(newProfileInfo.getTournamentWinCount());
        }
    }

    /**
     * Helper method to set default profile details.
     * 
     * @param profile the requested profile.
     * @param isOrganizer true if organizer.
     * @param isPlayer true if player.
     */
    private void setDetails(Profile profile, boolean isOrganizer, boolean isPlayer) {
        if (isOrganizer) {
            profile.setPoints(null);
            profile.setMatchCount(null);
            profile.setMatchWinCount(null);
            profile.setTournamentCount(null);
            profile.setTournamentWinCount(null);
        } else if (isPlayer) {
            profile.setPoints(1200);
            profile.setMatchCount(0);
            profile.setMatchWinCount(0);
            profile.setTournamentCount(0);
            profile.setTournamentWinCount(0);
            profile.setOrganization(null);
        }
    }

    /**
     * Helper method to filter out the list of profiles by players.
     * 
     * @param profileList the list of profiles from the repository.
     * @return a list of player profiles from the repository.
     */
    private List<Profile> filterByPlayers(List<Profile> profileList) {
        return profileList.stream()
                .filter(profile -> {
                    User user = profile.getUser();
                    return user != null && user.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"));
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper method to filter out the list of profiles by organisers.
     * 
     * @param profileList the list of profiles from the repository.
     * @return a list of organiser profiles from the repository.
     */
    private List<Profile> filterByOrganisers(List<Profile> profileList) {
        return profileList.stream()
        .filter(profile -> {
            User user = profile.getUser();
            return user != null && user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ORGANISER"));
        })
                .collect(Collectors.toList());
    }

    /**
     * Helper method to sort points based on points.
     * 
     * @param players the list of player profiles from the repository.
     */
    private void sort(List<Profile> players) {
        players.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
    }

    /**
     * Helper method to check for ties.
     * 
     * @param rankMap the current state of the rank map.
     * @param currentRank the rank to be assigned.
     * @param userId the requested userId.
     * @param i the current iteration.
     * @param p1 the points of the first player.
     * @param p2 the points of the second player.
     * @return the current rank if there are ties.
     */
    private int checkTies(Map<Long, Integer> rankMap, int currentRank, Long userId, int i, Integer p1, Integer p2) {
        if (i > 0 && p1.equals(p2)) {
            rankMap.put(userId, currentRank);
        } else {
            currentRank = i + 1;
            rankMap.put(userId, currentRank);
        }
        return currentRank;
    }

    /**
     * Helper method to add profiles to the list if not null.
     * 
     * @param user the requested user.
     * @param profiles the current list of profiles.
     */
    private void addProfileIfExists(User user, List<Profile> profiles) {
        if (user != null && (user.getProfile() != null)) {
            profiles.add(user.getProfile());
        }
    }

    /**
     * Helper method to validate that there are exactly two players in the match.
     * 
     * @param profiles the current list of players.
     * @param matchId the requested matchId.
     * @throws IllegalArgumentException if match does not have two players.
     */
    private void validatePlayersInMatch(List<Profile> players, Long matchId) {
        if (players.size() != 2) {
            throw new IllegalArgumentException("Match " + matchId + " does not have two players to calculate expected score.");
        }
    }

    /**
     * Helper method to get a list of points from profiles.
     * 
     * @param profiles the list of players.
     * @returns a list of points from profiles.
     */
    private List<Integer> getPointsFromProfiles(List<Profile> players) {
        List<Integer> retrieved = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Integer points = players.get(i).getPoints();
            retrieved.add(points);
        }
        return retrieved;
    }

    /**
     * Helper method to calculate the expected score.
     * 
     * @param playerPoints the points of the requested player.
     * @param opponentPoints the points of their opponent.
     * @return their expected chance of winning the match.
     */
    private double calculateExpectedScore(int playerPoints, int opponentPoints) {
        return 1.0 / (1 + Math.pow(10, (opponentPoints - playerPoints) / 400.0));
    }

    /**
     * Helper method to determine the expected score for the player in the match.
     * 
     * @param user the requested user.
     * @param players the list of players.
     * @param expectedScoreA the expected score of the requested player.
     * @param expectedScoreB the expected score of their opponent.
     * @return their expected chance of winning the match.
     */
    private double getPlayerExpectedScore(User user, List<Profile> players, double expectedScoreA, double expectedScoreB) {
        if (user.getProfile() == players.get(0)) {
            return expectedScoreA;
        } else {
            return expectedScoreB;
        }
    }

    /**
     * Helper method to validate that the winner is in the match.
     * 
     * @param winnerId the requested winnerId.
     * @param userId1 the first player in the match.
     * @param userId2 the second player in the match.
     * @throws IllegalArgumentException if the winner is not in the match.
     */
    private void validateWinner(Long winnerId, Long userId1, Long userId2) {
        if (winnerId != userId1 && winnerId != userId2) {
            throw new IllegalArgumentException("Player " + winnerId + " is not in the match.");
        }
    }

    /**
     * Helper method to update player statistics.
     * 
     * @param player the requested player.
     * @param originalPoints the original points of the player.
     * @param expectedScore the expected score of the player.
     * @param isWinner true if the player is the winner of the match.
     */
    private void updatePlayerStats(Profile player, Integer originalPoints, double expectedScore, boolean isWinner) {
        int K_FACTOR = 32;
        int result = isWinner ? 1 : 0;
        Integer newPoints = (int) (originalPoints + K_FACTOR * (result - expectedScore));
        player.setPoints(newPoints);

        // Update match win count for the winner.
        if (isWinner) {
            Integer matchWins = player.getMatchWinCount();
            player.setMatchWinCount(matchWins + 1);
        }
    }
}
