package csd.cuemaster.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchNotFoundException;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentNotFoundException;
import csd.cuemaster.tournament.TournamentRepository;

@Service
public class ProfileServiceImpl implements ProfileService{
    
    @Autowired
    private ProfileRepository profiles;
    @Autowired
    private UserRepository users;
    @Autowired
    private MatchRepository matches;
    @Autowired
    private TournamentRepository tournaments;

    @Override 
    public List<Profile> getAllProfile(){
        return profiles.findAll();
    }

    @Override 
    public Profile getProfile(Long userId, Long profileId) {
        if (!users.existsById(userId)){
            throw new UserNotFoundException(userId);
        }else if (!profiles.existsById(profileId)){
            throw new ProfileIdNotFoundException(profileId);
        }
        return profiles.findByUserId(userId)
                      .orElseThrow(()-> new UserProfileNotFoundException(userId));
    }

    //havent settle profile photo
    //can be used for PUT and POST method 
    @Override
    public Profile updateProfile(Long userId, Profile newProfileInfo){

        User user = users.findById(userId)       
                      .orElseThrow(() -> new UserNotFoundException(userId));

        return profiles.findByUserId(userId).map(profile -> {
            profile.setFirstname(newProfileInfo.getFirstname());
            profile.setLastname(newProfileInfo.getLastname());
            profile.setBirthdate(newProfileInfo.getBirthdate());
            profile.setBirthlocation(newProfileInfo.getBirthlocation());

            boolean isOrganizer = user.getAuthorities().stream()
                                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));  //getAuthorities return a Collections
            if (isOrganizer){
                if (newProfileInfo.getOrganization() == null || newProfileInfo.getOrganization().isEmpty()){
                    throw new OrganizationCannotBeNullException();
                }
                profile.setOrganization(newProfileInfo.getOrganization());
            }else {
                profile.setOrganization(null);
            }

            return profiles.save(profile);
        }).orElse(null);
    }

    @Override
    public Profile addProfile(User user, Profile profile){ 
        // User user = users.findById(userId)           
        //                 .orElseThrow(() -> new UserNotFoundException(userId));

        // profiles.findByUserId(userId).ifPresent(existingProfile ->{
        //                     throw new ProfileAlreadyExistsException(userId);});
        
        boolean isOrganizer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZER"));  //getAuthorities return a Collections
        boolean isPlayer = user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PLAYER"));

        profile.setUser(user);
        
        if (isOrganizer){
            profile.setPoints(null);
            profile.setMatchCount(null);
            profile.setMatchWinCount(null);
            profile.setTournamentCount(null);
            profile.setTournamentWinCount(null);
        }else if (isPlayer){
            profile.setPoints(1200);
            profile.setMatchCount(0);
            profile.setMatchWinCount(0);
            profile.setTournamentCount(0);
            profile.setTournamentWinCount(0);
            profile.setOrganization(null);
        }
        return profiles.save(profile);
    }

    // @Override
    // public String addProfilePhoto(Long userID, byte[] image){
    //     try{
    //         St
    //     }
    // } getAllProfile()

    // Return a list of all players.
    @Override
    public List<Profile> getPlayers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return profileList;
        }
        return profileList.stream()
                .filter(profile -> {
                    User user = profile.getUser();
                    return user != null && user.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"));
                })
                .collect(Collectors.toList());
    }

    // Sort all players based on points.
    @Override
    public List<Profile> sort() {
        List<Profile> profileList = getPlayers();
        profileList.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
        return profileList;
    }

    // Set all players ranks based on the sorted points.
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sort();
        Map<Long, Integer> rankMap = new HashMap<>();
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        Long playerId = currentPlayer.getId();
        rankMap.put(playerId, currentRank);
        for (int i = 1; i < sortedPlayers.size(); i++) {
            currentPlayer = sortedPlayers.get(i);
            playerId = currentPlayer.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            if (i > 0 && p1.equals(p2)) {
                rankMap.put(playerId, currentRank);
            } else {
                currentRank = i + 1;
                rankMap.put(playerId, currentRank);
            }
        }
        return rankMap;
    }

    // Set a player's points.
    @Override
    public Profile pointsSet(Long userId, Integer points) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Profile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException(userId));
        user = profile.getUser();
        if (user != null && user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"))) {
            profile.setPoints(points);
        }
        return profiles.save(profile);
    }

    // Retrieve player profiles from a given match.
    public List<Profile> getProfilesFromMatches(Long matchId) {
        Match match = matches.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
        List<Profile> retrieved = new ArrayList<>();
        addProfileIfExists(match.getUser1(), retrieved);
        addProfileIfExists(match.getUser2(), retrieved);
        return retrieved;
    }

    // Calculate the expected score of a given player in a given match.
    public double calculateExpectedScore(Long matchId, Long userId) {
        List<Profile> players = getProfilesFromMatches(matchId);
        validatePlayersInMatch(players, matchId);
        Integer pointsA = players.get(0).getPoints();
        Integer pointsB = players.get(1).getPoints();
        double expectedScoreA = calculateExpectedScore(pointsA, pointsB);
        double expectedScoreB = calculateExpectedScore(pointsB, pointsA);
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return getPlayerExpectedScore(user, players, expectedScoreA, expectedScoreB);
    }

    // Update player statistics after a winner is declared.
    public List<Profile> updatePlayerStatistics(Long matchId, Long winnerId) {
        Match match = matches.findById(matchId).orElseThrow(()-> new MatchNotFoundException(matchId));
        Long userId1 = match.getUser1().getId();
        Long userId2 = match.getUser2().getId();
        if (winnerId != userId1 && winnerId != userId2) {
            throw new IllegalArgumentException("Player " + winnerId + " is not in the match.");
        }
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

    // Retrieve player profiles from a given tournament.
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

    // Helper method to add profiles to the list if not null.
    private void addProfileIfExists(User user, List<Profile> profiles) {
        if (user != null && (user.getProfile() != null)) {
            profiles.add(user.getProfile());
        }
    }

    // Helper method to validate that there are exactly two players in the match.
    private void validatePlayersInMatch(List<Profile> players, Long matchId) {
        if (players.size() != 2) {
            throw new IllegalArgumentException("Match " + matchId + " does not have enough players to calculate expected score.");
        }
    }

    // Helper method to calculate the expected score.
    private double calculateExpectedScore(int playerPoints, int opponentPoints) {
        return 1.0 / (1 + Math.pow(10, (opponentPoints - playerPoints) / 400.0));
    }

    // Helper method to determine the expected score for the player in the match.
    private double getPlayerExpectedScore(User user, List<Profile> players, double expectedScoreA, double expectedScoreB) {
        if (user.getProfile() == players.get(0)) {
            return expectedScoreA;
        } else if (user.getProfile() == players.get(1)) {
            return expectedScoreB;
        } else {
            throw new IllegalArgumentException("Player " + user.getId() + " is not in the match.");
        }
    }

    // Helper method to update player statistics.
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