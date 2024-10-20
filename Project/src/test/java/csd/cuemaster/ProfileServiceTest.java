package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileAlreadyExistsException;
import csd.cuemaster.profile.ProfileIdNotFoundException;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileServiceImpl;
import csd.cuemaster.profile.UserProfileNotFoundException;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserNotFoundException;
import csd.cuemaster.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ProfileServicetest {

    @Mock
    private ProfileRepository profiles;
    @Mock
    private UserRepository users;

    @InjectMocks
    private ProfileServiceImpl profileService;

    // Test Case: Two profiles in the list.
    @Test
    void getAllProfiles_TwoProfiles_ReturnList() {
        // Arrange
        List<Profile> profileList = new ArrayList<>();

        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null);
        profile1.setId(1L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", "Cuesports", null);
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
            profileService.getProfile(1L, 1L);
        });

        // Assert
        assertEquals("User with UserID: 1 not found.", exception.getMessage());
    }

    // Test Case: Profile does not exist.
    @Test
    void getProfile_ProfileDoesNotExist_ThrowProfileIdNotFoundException() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);

        // Mock
        when(users.existsById(1L)).thenReturn(true);

        // Act
        ProfileIdNotFoundException exception = assertThrows(ProfileIdNotFoundException.class, () -> {
            profileService.getProfile(1L, 1L);
        });

        // Assert
        assertEquals("Profile ID: 1 not found.", exception.getMessage());
    }

    // Test Case: User profile does not exist.
    @Test
    void getProfile_UserProfileDoesNotExist_ThrowUserProfileNotFoundException() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile.setId(1L);

        // Mock
        when(users.existsById(1L)).thenReturn(true);
        when(profiles.existsById(1L)).thenReturn(true);

        // Act
        UserProfileNotFoundException exception = assertThrows(UserProfileNotFoundException.class, () -> {
            profileService.getProfile(1L, 1L);
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
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile.setId(1L);
        user.setProfile(profile);

        // Mock
        when(users.existsById(1L)).thenReturn(true);
        when(profiles.existsById(1L)).thenReturn(true);
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile));

        // Act
        Profile retrievedProfile = profileService.getProfile(1L, 1L);

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
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile1.setId(1L);
        user.setProfile(profile1);

        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile2.setId(1L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile1));
        when(profiles.save(any(Profile.class))).thenReturn(profile2);

        // Act
        Profile updatedProfile = profileService.updateProfile(1L, profile2);

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
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile.setId(1L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.save(any(Profile.class))).thenReturn(profile);

        // Act
        Profile addedProfile = profileService.addProfile(1L, profile);

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
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile1.setId(1L);
        user.setProfile(profile1);

        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", null);
        profile2.setId(2L);

        // Mock
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile1));

        // Act
        ProfileAlreadyExistsException exception = assertThrows(ProfileAlreadyExistsException.class, () -> {
            profileService.addProfile(1L, profile2);
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
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile.setId(1L);
        user.setProfile(profile);

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
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", "Cuesports", user);
        profile.setId(1L);
        user.setProfile(profile);

        profileList.add(profile);

        // Mock
        when(profiles.findAll()).thenReturn(profileList);

        // Act
        profileList = profileService.getPlayers();

        // Assert
        assertNotNull(profileList);
        assertTrue(profileList.isEmpty());
    }

    // Test Case: Update player points.
    @Test
    void pointsSet_UpdatePlayerPoints_ReturnProfileWithUpdatedPoints() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user);
        profile.setId(1L);
        user.setProfile(profile);

        // Mock
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profiles.save(any(Profile.class))).thenReturn(profile);

        // Act
        Profile updatedProfile = profileService.pointsSet(1L, 2300);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals(2300, profile.getPoints());
        verify(profiles).save(profile);
    }

    // Test Case: Attempt to update organiser points which does not exist.
    @Test
    void pointsSet_UpdateOrganiserPoints_ReturnProfileWithNullPoints() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", "Cuesports", user);
        profile.setId(1L);
        user.setProfile(profile);

        // Mock
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profiles.save(any(Profile.class))).thenReturn(profile);

        // Act
        Profile updatedProfile = profileService.pointsSet(1L, 2300);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals(null, profile.getPoints());
        verify(profiles).save(profile);
    }

    // Test Case: Sort players based on points.
    @Test
    void sort_UpdatePlayerPoints_ReturnSortedList() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", user1);
        profile1.setId(1L);
        profile1.setPoints(1200);
        user1.setProfile(profile1);

        User user2 = new User("Koopa", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", user2);
        profile2.setId(2L);
        profile2.setPoints(2300);
        user2.setProfile(profile2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        leaderboard = profileService.sort();

        // Assert
        assertNotNull(leaderboard);
        assertFalse(leaderboard.isEmpty());
        assertEquals(profile2, leaderboard.get(0));
    }

    // Test Case: Attempt to sort organisers which does not happen as they have no
    // points.
    @Test
    void sort_UpdateOrganiserPoints_ReturnEmptyList() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", "Cuesports", user1);
        profile1.setId(1L);
        user1.setProfile(profile1);
        User user2 = new User("Koopa", "goodpassword", "ROLE_ORGANISER", "normal", true);
        user2.setId(1L);
        Profile profile2 = new Profile("Koopa", "Troopa", LocalDate.of(2002, 7, 26), "Singapore", "Cuesports", user2);
        profile2.setId(1L);
        user2.setProfile(profile2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        leaderboard = profileService.sort();

        // Assert
        assertNotNull(leaderboard);
        assertTrue(leaderboard.isEmpty());
    }
}