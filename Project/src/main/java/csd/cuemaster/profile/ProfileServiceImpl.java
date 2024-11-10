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
import csd.cuemaster.services.ScoringService;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.Tournament.Status;
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

    @Autowired
    private ScoringService scoringService;

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


    //is called once a winner is declared in a match. 
    // Update player statistics when a match result is recorded
    public void updatePlayerStatistics(Profile winnerProfile, Profile loserProfile, Long tournamentId, Status matchStatus) {
        // Delegate rating calculation to ScoringService with match importance multiplier
        int newWinnerRating = scoringService.calculateNewRating(
            winnerProfile.getPoints(), 
            scoringService.calculateExpectedScore(winnerProfile.getPoints(), loserProfile.getPoints()), 
            1, // Winner's result is 1
            matchStatus, // Pass matchStatus to get the correct match importance multiplier
            winnerProfile.getMatchCount()
        );
        
        int newLoserRating = scoringService.calculateNewRating(
            loserProfile.getPoints(), 
            scoringService.calculateExpectedScore(loserProfile.getPoints(), winnerProfile.getPoints()), 
            0, // Loser's result is 0
            matchStatus, // Pass matchStatus to get the correct match importance multiplier
            loserProfile.getMatchCount()
        );

        // Update winner's and loser's points
        winnerProfile.setPoints(newWinnerRating);
        loserProfile.setPoints(newLoserRating);

        // Increment match and win counts
        incrementMatchCount(winnerProfile, true); // true indicates this player is the winner
        incrementMatchCount(loserProfile, false); // false indicates this player is the loser

        // Increment tournament counts for both players
        incrementTournamentCount(winnerProfile);
        incrementTournamentCount(loserProfile);

        // Save both profiles after updates
        profiles.save(winnerProfile);
        profiles.save(loserProfile);
    }
    // Helper to increment match counts
    private void incrementMatchCount(Profile profile, boolean isWinner) {
        profile.setMatchCount(profile.getMatchCount() + 1);
        if (isWinner) {
            profile.setMatchWinCount(profile.getMatchWinCount() + 1);
        }
    }

    // Helper to increment tournament counts
    private void incrementTournamentCount(Profile profile) {
        profile.setTournamentCount(profile.getTournamentCount() + 1);
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

    // Retrieve player profiles from a given tournament.
    @Override
    public List<Profile> getProfilesFromTournaments(Long tournamentId) {
        Tournament tournament = tournaments.findById(tournamentId).orElseThrow(()-> new TournamentNotFoundException(tournamentId));
        List<Profile> retrieved = new ArrayList<>();
        List<User> players = tournament.getPlayers();
        for (User player : players) {
            addProfileIfExists(player, retrieved);
        }
        return retrieved;
    }

    // Helper method to add profiles to the list if not null.
    private void addProfileIfExists(User user, List<Profile> profiles) {
        if (user != null && (user.getProfile() != null)) {
            profiles.add(user.getProfile());
        }
    }




}