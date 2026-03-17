package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    private ProfileService profileService;

    private Profile testProfile;
    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository);
        
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        
        testProfile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Test Profile")
                .avatarUrl(null)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getProfilesByUserId - Should return all profiles for user")
    void getProfilesByUserId_Success() {
        when(profileRepository.findByUserId(userId)).thenReturn(Flux.just(testProfile));

        StepVerifier.create(profileService.getProfilesByUserId(userId))
                .assertNext(response -> {
                    assertEquals(testProfile.getName(), response.getName());
                    assertEquals(testProfile.getId(), response.getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getProfileById - Should return profile by ID")
    void getProfileById_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Mono.just(testProfile));

        StepVerifier.create(profileService.getProfileById(profileId))
                .assertNext(response -> {
                    assertEquals(testProfile.getId(), response.getId());
                    assertEquals(testProfile.getName(), response.getName());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("createProfile - Should create profile when under limit")
    void createProfile_Success() {
        ProfileRequest request = new ProfileRequest();
        request.setName("New Profile");

        when(profileRepository.countByUserId(userId)).thenReturn(Mono.just(2L));
        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(testProfile));

        StepVerifier.create(profileService.createProfile(userId, request))
                .assertNext(response -> {
                    assertNotNull(response);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("createProfile - Should fail when at max profiles")
    void createProfile_MaxProfilesReached() {
        ProfileRequest request = new ProfileRequest();
        request.setName("New Profile");

        when(profileRepository.countByUserId(userId))
                .thenReturn(Mono.just((long) Profile.MAX_PROFILES_PER_USER));

        StepVerifier.create(profileService.createProfile(userId, request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("updateProfile - Should update profile name")
    void updateProfile_Success() {
        ProfileRequest request = new ProfileRequest();
        request.setName("Updated Profile");

        when(profileRepository.findById(profileId)).thenReturn(Mono.just(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(testProfile));

        StepVerifier.create(profileService.updateProfile(profileId, request))
                .assertNext(response -> {
                    assertNotNull(response);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteProfile - Should delete profile")
    void deleteProfile_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Mono.just(testProfile));
        when(profileRepository.deleteById(profileId)).thenReturn(Mono.empty());

        StepVerifier.create(profileService.deleteProfile(profileId))
                .verifyComplete();
    }

    @Test
    @DisplayName("belongsToUser - Should return true when profile belongs to user")
    void belongsToUser_True() {
        when(profileRepository.findById(profileId)).thenReturn(Mono.just(testProfile));

        StepVerifier.create(profileService.belongsToUser(profileId, userId))
                .assertNext(result -> assertTrue(result))
                .verifyComplete();
    }

    @Test
    @DisplayName("belongsToUser - Should return false when profile belongs to different user")
    void belongsToUser_False() {
        UUID differentUserId = UUID.randomUUID();
        
        when(profileRepository.findById(profileId)).thenReturn(Mono.just(testProfile));

        StepVerifier.create(profileService.belongsToUser(profileId, differentUserId))
                .assertNext(result -> assertFalse(result))
                .verifyComplete();
    }

    @Test
    @DisplayName("belongsToUser - Should return false when profile not found")
    void belongsToUser_NotFound() {
        when(profileRepository.findById(profileId)).thenReturn(Mono.empty());

        StepVerifier.create(profileService.belongsToUser(profileId, userId))
                .assertNext(result -> assertFalse(result))
                .verifyComplete();
    }
}
