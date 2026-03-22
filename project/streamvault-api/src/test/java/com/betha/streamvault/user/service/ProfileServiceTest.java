package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.ProfileJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock
    private ProfileJpaRepository profileJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    private ProfileService profileService;

    private Profile testProfile;
    private User testUser;
    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileJpaRepository, userJpaRepository);
        
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(userId)
                .email("test@streamvault.com")
                .name("Test User")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();
        
        testProfile = Profile.builder()
                .id(profileId)
                .user(testUser)
                .name("Test Profile")
                .avatarUrl(null)
                .build();
    }

    @Test
    @DisplayName("getProfilesByUserId - Should return all profiles for user")
    void getProfilesByUserId_Success() {
        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(java.util.List.of(testProfile));

        var result = profileService.getProfilesByUserId(userId);

        assertFalse(result.isEmpty());
        assertEquals(testProfile.getName(), result.get(0).getName());
        assertEquals(testProfile.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("getProfileById - Should return profile by ID")
    void getProfileById_Success() {
        when(profileJpaRepository.findById(profileId)).thenReturn(Optional.of(testProfile));

        var result = profileService.getProfileById(profileId);

        assertTrue(result.isPresent());
        assertEquals(testProfile.getId(), result.get().getId());
        assertEquals(testProfile.getName(), result.get().getName());
    }

    @Test
    @DisplayName("createProfile - Should create profile when under limit")
    void createProfile_Success() {
        ProfileRequest request = new ProfileRequest();
        request.setName("New Profile");

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileJpaRepository.countByUser(testUser)).thenReturn(2L);
        when(profileJpaRepository.save(any(Profile.class))).thenReturn(testProfile);

        var result = profileService.createProfile(userId, request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("createProfile - Should fail when at max profiles")
    void createProfile_MaxProfilesReached() {
        ProfileRequest request = new ProfileRequest();
        request.setName("New Profile");

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileJpaRepository.countByUser(testUser)).thenReturn((long) Profile.MAX_PROFILES_PER_USER);

        assertThrows(IllegalArgumentException.class, () -> profileService.createProfile(userId, request));
    }

    @Test
    @DisplayName("updateProfile - Should update profile name")
    void updateProfile_Success() {
        ProfileRequest request = new ProfileRequest();
        request.setName("Updated Profile");

        when(profileJpaRepository.findById(profileId)).thenReturn(Optional.of(testProfile));
        when(profileJpaRepository.save(any(Profile.class))).thenReturn(testProfile);

        var result = profileService.updateProfile(profileId, request);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("deleteProfile - Should delete profile")
    void deleteProfile_Success() {
        // deleteById is called directly, no findById needed
        assertDoesNotThrow(() -> profileService.deleteProfile(profileId));
    }

    @Test
    @DisplayName("belongsToUser - Should return true when profile belongs to user")
    void belongsToUser_True() {
        when(profileJpaRepository.findById(profileId)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.belongsToUser(profileId, userId);

        assertTrue(result);
    }

    @Test
    @DisplayName("belongsToUser - Should return false when profile belongs to different user")
    void belongsToUser_False() {
        UUID differentUserId = UUID.randomUUID();
        
        when(profileJpaRepository.findById(profileId)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.belongsToUser(profileId, differentUserId);

        assertFalse(result);
    }

    @Test
    @DisplayName("belongsToUser - Should return false when profile not found")
    void belongsToUser_NotFound() {
        when(profileJpaRepository.findById(profileId)).thenReturn(Optional.empty());

        boolean result = profileService.belongsToUser(profileId, userId);

        assertFalse(result);
    }
}
