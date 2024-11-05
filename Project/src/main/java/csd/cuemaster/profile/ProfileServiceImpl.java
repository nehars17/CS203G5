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

    // Returns a list of all players.
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

    // Sorts all players based on points.
    @Override
    public List<Profile> sort() {
        List<Profile> profileList = getPlayers();
        profileList.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
        return profileList;
    }

    // Sets all players ranks based on the sorted points.
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sort();
        Map<Long, Integer> rankMap = new HashMap<>();
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        Long player_id = currentPlayer.getId();
        rankMap.put(player_id, currentRank);
        for (int i = 1; i < sortedPlayers.size(); i++) {
            currentPlayer = sortedPlayers.get(i);
            player_id = currentPlayer.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            if (i > 0 && p1.equals(p2)) {
                rankMap.put(player_id, currentRank);
            } else {
                currentRank = i + 1;
                rankMap.put(player_id, currentRank);
            }
        }
        return rankMap;
    }

    // Sets a player's points.
    @Override
    public Profile pointsSet(Long user_id, Integer points) {
        User user = users.findById(user_id)
                .orElseThrow(() -> new UserNotFoundException(user_id));
        Profile profile = profiles.findByUserId(user_id)
                .orElseThrow(() -> new UserProfileNotFoundException(user_id));
        user = profile.getUser();
        if (user != null && user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"))) {
            profile.setPoints(points);
        }
        return profiles.save(profile);
    }

    // Retrieves player profiles from a given match.
    public List<Profile> getProfilesFromMatches(Long match_id) {
        Match match = matches.findById(match_id).orElseThrow(()-> new MatchNotFoundException(match_id));
        List<Profile> retrieved = new ArrayList<>();
        User user1 = match.getUser1();
        if (user1 != null) {
            Profile profile1 = user1.getProfile();
                if (profile1 != null) {
                    retrieved.add(profile1);
                }
        }
        User user2 = match.getUser2();
        if (user2 != null) {
            Profile profile2 = user2.getProfile();
                if (profile2 != null) {
                    retrieved.add(profile2);
                }
        }
        return retrieved;
    }

    // Calculate the expected score of a given player in a given match.
    public double calculateExpectedScore(Long match_id, Long user_id) {
        List<Profile> players = getProfilesFromMatches(match_id);
        if (players.size() < 2) {
            throw new IllegalArgumentException("Match " + match_id + " does not have enough players to calculate expected score.");
        }
        Integer pointsA = players.get(0).getPoints();
        Integer pointsB = players.get(1).getPoints();
        double expectedScoreA = 1.0 / (1 + Math.pow(10, (pointsB - pointsA) / 400.0));
        double expectedScoreB = 1.0 / (1 + Math.pow(10, (pointsA - pointsB) / 400.0));
        User user = users.findById(user_id)
                .orElseThrow(() -> new UserNotFoundException(user_id));
        if (user.getProfile() == players.get(0)) {
            return expectedScoreA;
        } else if (user.getProfile() == players.get(1)) {
            return expectedScoreB;
        } else {
            throw new IllegalArgumentException("Player " + user_id + " is not in the match.");
        }
    }

    // Update player statistics after a winner is declared.
    public List<Profile> updatePlayerStatistics(Long match_id, Long winner_id) {
        Match match = matches.findById(match_id).orElseThrow(()-> new MatchNotFoundException(match_id));
        Long user_id1 = match.getUser1().getId();
        Long user_id2 = match.getUser2().getId();
        if (winner_id != user_id1 && winner_id != user_id2) {
            throw new IllegalArgumentException("Player " + winner_id + " is not in the match.");
        }
        List<Profile> players = getProfilesFromMatches(match_id);
        Integer originalPointsA = players.get(0).getPoints();
        Integer originalPointsB = players.get(1).getPoints();
        double expectedScoreA = calculateExpectedScore(match_id, user_id1);
        double expectedScoreB = calculateExpectedScore(match_id, user_id2);

        // Update player statistics based on the winner.
        if (winner_id == user_id1) {
            updatePlayerStats(players.get(0), originalPointsA, expectedScoreA, true);
            updatePlayerStats(players.get(1), originalPointsB, expectedScoreB, false);
        } else if (winner_id == user_id2) {
            updatePlayerStats(players.get(0), originalPointsA, expectedScoreA, false);
            updatePlayerStats(players.get(1), originalPointsB, expectedScoreB, true);
        }
        profiles.saveAll(players);
        return players;
    }

    // Helper method for updating player statistics.
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

    // Retrieves player profiles from a given tournament.
    public List<Profile> getProfilesFromTournaments(Long tournament_id) {
        Tournament tournament = tournaments.findById(tournament_id).orElseThrow(()-> new TournamentNotFoundException(tournament_id));
        List<Profile> retrieved = new ArrayList<>();
        List<Long> players = tournament.getPlayers();
        for (Long player : players) {
            User user = users.findById(player).orElseThrow(()-> new UserNotFoundException(player));
            Profile profile = user.getProfile();
            if (profile != null) {
                retrieved.add(profile);
            }
        }
        return retrieved;
    }
}