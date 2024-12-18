package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileAlreadyExistsException;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileServiceImpl;
import csd.cuemaster.profile.UserProfileNotFoundException;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.imageservice.ImageService;
import csd.cuemaster.match.Match;
import csd.cuemaster.match.MatchNotFoundException;
import csd.cuemaster.match.MatchRepository;
import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.tournament.TournamentNotFoundException;
import csd.cuemaster.tournament.TournamentRepository;

@ExtendWith(MockitoExtension.class)
public class ProfileServicetest {

    @Mock
    private ProfileRepository profiles;
    @Mock
    private UserRepository users;
    @Mock
    private MatchRepository matches;
    @Mock
    private TournamentRepository tournaments;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    // Test Case: Two profiles in the list.
    @Test
    void getAllProfiles_TwoProfiles_ReturnList() {
        // Arrange
        List<Profile> profileList = new ArrayList<>();

        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, null);
        profile1.setId(1L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, null);
        profile2.setId(2L);

        profileList.add(profile1);
        profileList.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(profileList);

        // Act
        profileList = profileService.getAllProfile();

        // Assert
        assertNotNull(profileList);
        assertFalse(profileList.isEmpty());
    }

    // Test Case: User does not exist.
    @Test
    void getProfile_UserDoesNotExist_ThrowUserNotFoundException() {
        // Arrange (Nothing to arrange.)

        // Mock (Nothing to mock.)

        // Act
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            profileService.getProfile(1L);
        });

        // Assert
        assertEquals("User with UserID: 1 not found.", exception.getMessage());
    }

    // Test Case: User profile does not exist.
    @Test
    void getProfile_UserProfileDoesNotExist_ThrowUserProfileNotFoundException() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);

        // Mock
        when(users.existsById(1L)).thenReturn(true);

        // Act
        UserProfileNotFoundException exception = assertThrows(UserProfileNotFoundException.class, () -> {
            profileService.getProfile(1L);
        });

        // Assert
        assertEquals("User with User ID: 1 does not have a profile.", exception.getMessage());
    }

    // Test Case: Retrieve a user profile.
    @Test
    void getProfile_UserProfile_ReturnUserProfile() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);
        user.setProfile(profile);

        // Mock
        when(users.existsById(1L)).thenReturn(true);
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile));

        // Act
        Profile retrievedProfile = profileService.getProfile(1L);

        // Assert
        assertNotNull(retrievedProfile);
        assertEquals(profile, retrievedProfile);
        assertEquals("Glenn", retrievedProfile.getFirstname());
    }

    // Test Case: Change a player profile.
    @Test
    void updateProfile_ChangePlayerProfile_ReturnUpdatedProfile() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile1.setId(1L);
        user.setProfile(profile1);

        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile2.setId(1L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile1));
        when(profiles.save(any(Profile.class))).thenReturn(profile2);

        // Act
        Profile updatedProfile = profileService.updateProfile(1L, profile2, null);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals(profile2, updatedProfile);
        assertEquals("Koopa", updatedProfile.getFirstname());
    }

    // Test Case: Add a player profile.
    @Test
    void addProfile_PlayerProfile_ReturnProfile() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);

        byte[] content = "dummy content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", "dummy.txt", "text/plain", content);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.save(any(Profile.class))).thenReturn(profile);
        when(imageService.saveImage(anyLong(), any(MultipartFile.class))).thenReturn("image-path");

        // Act
        Profile addedProfile = profileService.addProfile(1L, profile, multipartFile);

        // Assert
        assertNotNull(addedProfile);
        assertEquals(profile, addedProfile);
        assertEquals(1200, profile.getPoints());
    }

    // Test Case: Add a player profile that already belongs to a user.
    @Test
    void addProfile_PlayerWithProfile_ThrowProfileAlreadyExistsException() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile1.setId(1L);
        user.setProfile(profile1);

        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, null);
        profile2.setId(2L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));

        // Act
        ProfileAlreadyExistsException exception = assertThrows(ProfileAlreadyExistsException.class, () -> {
            profileService.addProfile(1L, profile2, null);
        });

        // Assert
        assertEquals("User ID: 1 profile already exists.", exception.getMessage());
    }

    // Test Case: One player in the list.
    @Test
    void getPlayers_Player_ReturnListWithPlayer() {
        // Arrange
        List<Profile> profileList = new ArrayList<>();

        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);
        profile.setUser(user);

        profileList.add(profile);

        // Mock
        when(profiles.findAll()).thenReturn(profileList);

        // Act
        profileList = profileService.getPlayers();

        // Assert
        assertNotNull(profileList);
        assertFalse(profileList.isEmpty());
    }

    // Test Case: One organiser in the list.
    @Test
    void getPlayers_Organiser_ReturnEmptyList() {
        // Arrange
        List<Profile> profileList = new ArrayList<>();

        User user = new User("Glenn", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore",null, "SMU", user);
        profile.setId(1L);
        profile.setUser(user);

        profileList.add(profile);

        // Mock
        when(profiles.findAll()).thenReturn(profileList);

        // Act
        profileList = profileService.getPlayers();

        // Assert
        assertNotNull(profileList);
        assertTrue(profileList.isEmpty());
    }

    // Test Case: Sort players based on points.
    @Test
    void sortProfiles_UpdatePlayerPoints_ReturnSortedList() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        profile1.setPoints(1200);
        profile1.setUser(user1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        profile2.setPoints(2300);
        profile2.setUser(user2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        leaderboard = profileService.sortProfiles();

        // Assert
        assertNotNull(leaderboard);
        assertFalse(leaderboard.isEmpty());
        assertEquals(profile2, leaderboard.get(0));
    }

    // Test Case: Attempt to sort organisers which does not happen as they have no
    // points.
    @Test
    void sortProfiles_UpdateOrganiserPoints_ReturnEmptyList() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, "SMU", user1);
        profile1.setId(1L);
        profile1.setUser(user1);
        User user2 = new User("Koopa", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, "SMU", user2);
        profile2.setId(2L);
        profile2.setUser(user2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        leaderboard = profileService.sortProfiles();

        // Assert
        assertNotNull(leaderboard);
        assertTrue(leaderboard.isEmpty());
    }

    // Test Case: Set player ranks based on points.
    @Test
    void setRank_SetPlayerRanks_ReturnRankMap() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        profile1.setPoints(1200);
        profile1.setUser(user1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        profile2.setPoints(2300);
        profile2.setUser(user2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        Map<Long, Integer> rankMap = profileService.setRank();

        // Assert
        assertNotNull(rankMap);
        assertFalse(rankMap.isEmpty());
        assertEquals(2, rankMap.get(1L));
        assertEquals(1, rankMap.get(2L));
    }

    // Test Case: Players with same points.
    @Test
    void setRank_SamePoints_ReturnRankMap() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        profile1.setPoints(1200);
        profile1.setUser(user1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        profile2.setPoints(2300);
        profile2.setUser(user2);

        User user3 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user3.setId(3L);
        Profile profile3 = new Profile("Koopa", "Paratroopa", LocalDate.of(2002, 7, 26), "Singapore", null, user3);
        profile3.setId(3L);
        profile3.setPoints(2300);
        profile3.setUser(user3);

        leaderboard.add(profile1);
        leaderboard.add(profile2);
        leaderboard.add(profile3);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        Map<Long, Integer> rankMap = profileService.setRank();

        // Assert
        assertNotNull(rankMap);
        assertFalse(rankMap.isEmpty());
        assertEquals(3, rankMap.get(1L));
        assertEquals(1, rankMap.get(2L));
        assertEquals(1, rankMap.get(3L));
    }

    // Test Case: No players.
    @Test
    void setRank_NoPlayers_ReturnEmptyMap() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        Map<Long, Integer> rankMap = profileService.setRank();

        // Assert
        assertNotNull(rankMap);
        assertTrue(rankMap.isEmpty());
    }

    // Test Case: Get two player profiles from a match.
    @Test
    void getProfilesFromMatches_TwoPlayerProfiles_ReturnList() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromMatches(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertFalse(retrievedProfiles.isEmpty());
        assertEquals(2, retrievedProfiles.size());
    }

    // Test Case: Get one player profile from a match.
    @Test
    void getProfilesFromMatches_OnePlayerProfile_ReturnList() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);
        user.setProfile(profile);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromMatches(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertFalse(retrievedProfiles.isEmpty());
        assertEquals(1, retrievedProfiles.size());
    }

    // Test Case: No player profiles.
    @Test
    void getProfilesFromMatches_ZeroPlayerProfiles_ReturnEmptyList() {
        // Arrange
        Match match = new Match();
        match.setId(1L);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromMatches(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertTrue(retrievedProfiles.isEmpty());
    }

    // Test Case: Match does not exist.
    @Test
    void getProfilesFromMatches_MatchDoesNotExist_ThrowMatchNotFoundException() {
        // Arrange (Nothing to arrange.)

        // Mock (Nothing to mock.)

        // Act
        MatchNotFoundException exception = assertThrows(MatchNotFoundException.class, () -> {
            profileService.getProfilesFromMatches(1L);
        });

        // Assert
        assertEquals("Could not find match 1.", exception.getMessage());
    }

    // Test Case: Get the expected score of a player from a match.
    @Test
    void calculateExpectedScore_PlayerA_ReturnScore() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);
        profile1.setPoints(1200);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);
        profile2.setPoints(2300);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));
        when(users.findById(1L)).thenReturn(Optional.of(user1));

        // Act
        double score = profileService.calculateExpectedScore(1L, 1L);

        // Assert
        assertNotNull(score);
        assertEquals(0.0017751227458097578, score);
    }

    // Test Case: Not enough players in a match.
    @Test
    void calculateExpectedScore_NotEnoughPlayers_ThrowIllegalArgumentException() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));
        when(users.findById(1L)).thenReturn(Optional.of(user1));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.calculateExpectedScore(1L, 1L);
        });

        // Assert
        assertEquals("Match 1 does not have two players to calculate expected score.", exception.getMessage());
    }

    // Test Case: Player does not exist in a match.
    @Test
    void calculateExpectedScore_PlayerNotFound_ThrowIllegalArgumentException() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);

        User user3 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user3.setId(3L);
        Profile profile3 = new Profile("Koopa", "Paratroopa", LocalDate.of(2002, 7, 26), "Singapore", null, user3);
        profile3.setId(3L);
        user3.setProfile(profile3);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));
        when(users.findById(3L)).thenReturn(Optional.of(user3));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.calculateExpectedScore(1L, 3L);
        });

        // Assert
        assertEquals("Player 3 is not in the match.", exception.getMessage());
    }

    // Test Case: Get the new points of the players after a match.
    @Test
    void updatePlayerStatistics_PlayerAWins_ReturnList() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);
        profile1.setPoints(1200);
        profile1.setMatchWinCount(0);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);
        profile2.setPoints(2300);
        profile2.setMatchWinCount(0);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));
        when(users.findById(1L)).thenReturn(Optional.of(user1));
        when(users.findById(2L)).thenReturn(Optional.of(user2));

        // Act
        List<Profile> updatedProfiles = profileService.updatePlayerStatistics(1L, 1L);

        // Assert
        assertNotNull(updatedProfiles);
        assertEquals(1231, updatedProfiles.get(0).getPoints());
        assertEquals(2268, updatedProfiles.get(1).getPoints());
        assertEquals(1, updatedProfiles.get(0).getMatchWinCount());
        assertEquals(0, updatedProfiles.get(1).getMatchWinCount());
    }

    // Test Case: Winner does not exist in a match.
    @Test
    void updatePlayerStatistics_WinnerNotFound_ThrowIllegalArgumentException() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);

        Match match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);

        // Mock
        when(matches.findById(1L)).thenReturn(Optional.of(match));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.updatePlayerStatistics(1L, 3L);
        });

        // Assert
        assertEquals("Player 3 is not in the match.", exception.getMessage());
    }

    // Test Case: Get two player profiles from a tournament.
    @Test
    void getProfilesFromTournaments_TwoPlayerProfiles_ReturnList() {
        // Arrange
        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.getPlayers().add(1L);
        tournament.getPlayers().add(2L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user1));
        when(users.findById(2L)).thenReturn(Optional.of(user2));
        when(tournaments.findById(1L)).thenReturn(Optional.of(tournament));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromTournaments(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertFalse(retrievedProfiles.isEmpty());
        assertEquals(2, retrievedProfiles.size());
    }

    // Test Case: Get one player profile from a tournament.
    @Test
    void getProfilesFromTournaments_OnePlayerProfile_ReturnList() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);
        user.setProfile(profile);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.getPlayers().add(1L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(tournaments.findById(1L)).thenReturn(Optional.of(tournament));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromTournaments(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertFalse(retrievedProfiles.isEmpty());
        assertEquals(1, retrievedProfiles.size());
    }

    // Test Case: No player profiles.
    @Test
    void getProfilesFromTournaments_ZeroPlayerProfiles_ReturnEmptyList() {
        // Arrange
        Tournament tournament = new Tournament();
        tournament.setId(1L);

        // Mock
        when(tournaments.findById(1L)).thenReturn(Optional.of(tournament));

        // Act
        List<Profile> retrievedProfiles = profileService.getProfilesFromTournaments(1L);

        // Assert
        assertNotNull(retrievedProfiles);
        assertTrue(retrievedProfiles.isEmpty());
    }

    // Test Case: Tournament does not exist.
    @Test
    void getProfilesFromTournaments_TournamentDoesNotExist_ThrowTournamentNotFoundException() {
        // Arrange (Nothing to arrange.)

        // Mock (Nothing to mock.)

        // Act
        TournamentNotFoundException exception = assertThrows(TournamentNotFoundException.class, () -> {
            profileService.getProfilesFromTournaments(1L);
        });

        // Assert
        assertEquals("Could not find tournament 1", exception.getMessage());
    }
}