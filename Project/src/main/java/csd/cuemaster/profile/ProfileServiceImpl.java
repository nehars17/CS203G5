package csd.cuemaster.profile;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.imageservice.ImageService;
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
    private ImageService imageService;
    
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
    public Profile getProfile(Long userId) {
        if (!users.existsById(userId)){
            throw new UserNotFoundException(userId);
        }
        return profiles.findByUserId(userId)
                      .orElseThrow(()-> new UserProfileNotFoundException(userId));
    }

    //havent settle profile photo
    //can be used for PUT and POST method 
    @Override
    public Profile updateProfile(Long userId, Profile newProfileInfo, MultipartFile profilephoto){

        User user = users.findById(userId)       
                      .orElseThrow(() -> new UserNotFoundException(userId));

        return profiles.findByUserId(userId).map(profile -> {
            profile.setFirstname(newProfileInfo.getFirstname());
            profile.setLastname(newProfileInfo.getLastname());
            profile.setBirthdate(newProfileInfo.getBirthdate());
            profile.setBirthlocation(newProfileInfo.getBirthlocation());

            if(profilephoto != null && !profilephoto.isEmpty()){

                String photoPath = imageService.saveImage(user.getId(), profilephoto);
                profile.setProfilephotopath(photoPath);
            }

            boolean isOrganizer = user.getAuthorities().stream()
                                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANISER"));  //getAuthorities return a Collections
            if (isOrganizer){
                if (newProfileInfo.getOrganization() == null || newProfileInfo.getOrganization().isEmpty()){
                    throw new OrganizationCannotBeNullException();
                }
                profile.setOrganization(newProfileInfo.getOrganization());
            }else {
                profile.setOrganization(null);
                profile.setMatchCount(newProfileInfo.getMatchCount());
                profile.setMatchWinCount(newProfileInfo.getMatchWinCount());
                profile.setTournamentCount(newProfileInfo.getTournamentCount());
                profile.setTournamentWinCount(newProfileInfo.getTournamentWinCount());
            }

            return profiles.save(profile);
        }).orElse(null);
    }

    @Override
    public Profile addProfile(User user, Profile profile, MultipartFile profilephoto){ 
        // User user = users.findById(userId)           
        //                 .orElseThrow(() -> new UserNotFoundException(userId));

        // profiles.findByUserId(userId).ifPresent(existingProfile ->{
        //                     throw new ProfileAlreadyExistsException(userId);});

        if(profilephoto != null && !profilephoto.isEmpty()){

            String photoPath = imageService.saveImage(user.getId(), profilephoto);
            profile.setProfilephotopath(photoPath);
        }else {
            throw new ProfilePhotoRequiredException();
        }
        
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

    @Override
    public List<Profile> getOrganisers() {
        List<Profile> profileList = profiles.findAll();
        if (profileList == null || profileList.isEmpty()) {
            return new ArrayList<>();
        }
        return profileList.stream()
        .filter(profile -> {
            User user = profile.getUser();
            return user != null && user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ORGANISER"));
        })
                .collect(Collectors.toList());
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

    // Sort all players based on points.
    @Override
    public List<Profile> sortProfiles() {
        List<Profile> profileList = getPlayers();
        sort(profileList);
        return profileList;
    }

    // Set all players ranks based on the sorted points.
    @Override
    public Map<Long, Integer> setRank() {
        List<Profile> sortedPlayers = sortProfiles();
        Map<Long, Integer> rankMap = new HashMap<>();
        if (sortedPlayers == null || sortedPlayers.isEmpty()) {
            return rankMap;
        }
        int currentRank = 1;
        Profile currentPlayer = sortedPlayers.get(0);
        User currentUser = currentPlayer.getUser();
        Long userId = currentUser.getId();
        rankMap.put(userId, currentRank);
        for (int i = 1; i < sortedPlayers.size(); i++) {
            currentPlayer = sortedPlayers.get(i);
            currentUser = currentPlayer.getUser();
            userId = currentUser.getId();
            Integer p1 = currentPlayer.getPoints();
            Integer p2 = sortedPlayers.get(i - 1).getPoints();

            // Check for ties.
            if (i > 0 && p1.equals(p2)) {
                rankMap.put(userId, currentRank);
            } else {
                currentRank = i + 1;
                rankMap.put(userId, currentRank);
            }
        }
        return rankMap;
    }

    @Override
    public String getName(long user_id){
        User user = users.findById(user_id)           
                        .orElseThrow(() -> new UserNotFoundException(user_id));

        // Retrieve the profile using the user_id
        Profile profile = profiles.findByUserId(user_id)
                                .orElseThrow(() -> new ProfileIdNotFoundException(user_id));    // Handle case where profile is not found

        String fullname = profile.getFirstname() + " " + profile.getLastname();
        return fullname;
    }

    public void increaseTournamentCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int tournamentcount = profile.getTournamentCount() + 1;
        profile.setTournamentCount(tournamentcount);
        profiles.save(profile);
    }

    public void TournamentWinCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int tournamentWincount = profile.getTournamentWinCount() + 1;
        profile.setTournamentWinCount(tournamentWincount);
        profiles.save(profile);
    }

    public void increaseMatchCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int matchcount = profile.getMatchCount() + 1;
        profile.setMatchCount(matchcount);
        profiles.save(profile);
    }

    public void MatchWinCount(Long userId){

        User user = users.findById(userId)           
                        .orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profiles.findByUserId(userId)
                        .orElseThrow(() -> new ProfileAlreadyExistsException(userId));
        
        int matchWincount = profile.getMatchWinCount() + 1;
        profile.setTournamentWinCount(matchWincount);
        profiles.save(profile);
    }

    // Retrieve player profiles from a given match.
    @Override
    public List<Profile> getProfilesFromMatches(Long matchId) {
        Match match = matches.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
        List<Profile> retrieved = new ArrayList<>();
        addProfileIfExists(match.getUser1(), retrieved);
        addProfileIfExists(match.getUser2(), retrieved);
        return retrieved;
    }

    // Calculate the expected score of a given player in a given match.
    @Override
    public double calculateExpectedScore(Long matchId, Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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

    // Update player statistics after a winner is declared.
    @Override
    public List<Profile> updatePlayerStatistics(Long matchId, Long winnerId) {
        Match match = matches.findById(matchId).orElseThrow(()-> new MatchNotFoundException(matchId));
        Long userId1 = match.getUser1().getId();
        Long userId2 = match.getUser2().getId();
        validateWinner(winnerId, userId1, userId2);
        List<Profile> players = getProfilesFromMatches(matchId);
        validatePlayersInMatch(players, matchId);
        List<Integer> points = getPointsFromProfiles(players);
        Integer originalPointsA = points.get(0);
        Integer originalPointsB = points.get(1);
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

    // Helper method to sort points based on points.
    private void sort(List<Profile> players) {
        players.sort(Comparator.comparingInt(profile -> ((Profile) profile).getPoints()).reversed());
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
            throw new IllegalArgumentException("Match " + matchId + " does not have two players to calculate expected score.");
        }
    }

    // Helper method to get a list of points from profiles.
    private List<Integer> getPointsFromProfiles(List<Profile> players) {
        List<Integer> retrieved = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Integer points = players.get(i).getPoints();
            retrieved.add(points);
        }
        return retrieved;
    }

    // Helper method to calculate the expected score.
    private double calculateExpectedScore(int playerPoints, int opponentPoints) {
        return 1.0 / (1 + Math.pow(10, (opponentPoints - playerPoints) / 400.0));
    }

    // Helper method to determine the expected score for the player in the match.
    private double getPlayerExpectedScore(User user, List<Profile> players, double expectedScoreA, double expectedScoreB) {
        if (user.getProfile() == players.get(0)) {
            return expectedScoreA;
        } else {
            return expectedScoreB;
        }
    }

    // Helper method to validate that the winner is in the match.
    private void validateWinner(Long winnerId, Long userId1, Long userId2) {
        if (winnerId != userId1 && winnerId != userId2) {
            throw new IllegalArgumentException("Player " + winnerId + " is not in the match.");
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