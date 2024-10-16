package csd.cuemaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import csd.cuemaster.profile.Profile;
import csd.cuemaster.profile.ProfileRepository;
import csd.cuemaster.profile.ProfileServiceImpl;
import csd.cuemaster.profile.UserProfileNotFoundException;
import csd.cuemaster.user.User;
import csd.cuemaster.user.UserRepository;
import csd.cuemaster.user.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profiles;
    @Mock
    private UserRepository users;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void createUserWithProfile() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
        profile.setId(1L);
        user.setProfile(profile);

        // Act & Assert
        assertNotNull(user.getProfile());
        assertEquals("Glenn", user.getProfile().getUser().getUsername());
    }

    @Test
    void pointsSet_UpdatePoints_ReturnProfileWithUpdatedPoints() {
        // Arrange
        User user = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user.setId(1L);
        Profile profile = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user);
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
    }

    @Test
    void sort_UpdatePoints_ReturnListWithUpdatedPoints() {
        // Arrange
        List<Profile> leaderboard = new ArrayList<>();

        User user1 = new User("Glenn", "goodpassword", "ROLE_PLAYER", "normal", true);
        user1.setId(1L);
        Profile profile1 = new Profile("Glenn", "Fan", LocalDate.of(2002, 7, 26), "Singapore", null, user1);
        profile1.setId(1L);
        user1.setProfile(profile1);

        User user2 = new User("Katie", "goodpassword", "ROLE_PLAYER", "normal", true);
        user2.setId(2L);
        Profile profile2 = new Profile("Katie", "Cutie", LocalDate.of(2002, 7, 26), "Singapore", null, user2);
        profile2.setId(2L);
        user2.setProfile(profile2);

        leaderboard.add(profile1);
        leaderboard.add(profile2);

        // Mock
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(profile1));
        when(profiles.findByUserId(2L)).thenReturn(Optional.of(profile2));
        when(profiles.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.findAll()).thenReturn(leaderboard);

        // Act
        profile1 = profileService.pointsSet(1L, 1200);
        profile2 = profileService.pointsSet(2L, 2300);
        leaderboard = profileService.sort();

        // Assert
        assertNotNull(leaderboard);
        assertFalse(leaderboard.isEmpty());
        assertEquals(1200, profile1.getPoints());
        assertEquals(2300, profile2.getPoints());
        assertEquals(profile2, leaderboard.get(0));
    }
}