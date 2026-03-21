package com.betha.streamvault.history.service;

import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.model.WatchHistory;
import com.betha.streamvault.history.repository.WatchHistoryRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.ProfileRepository;
import com.betha.streamvault.user.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryService Tests")
class HistoryServiceTest {

    @Mock
    private WatchHistoryRepository watchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    private HistoryService historyService;

    private User testUser;
    private Profile testProfile;
    private WatchHistory testHistory;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(watchHistoryRepository, userRepository, profileRepository);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role("ROLE_USER")
                .isVerified(true)
                .createdAt(Instant.now())
                .build();

        testProfile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .name("Test Profile")
                .createdAt(Instant.now())
                .build();

        testHistory = WatchHistory.builder()
                .id(UUID.randomUUID())
                .profileId(testProfile.getId())
                .episodeId(UUID.randomUUID())
                .progressSec(120)
                .watchedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getActiveProfile - Should return profile for user")
    void getActiveProfile_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));

        Mono<Profile> result = historyService.getActiveProfile(testUser.getEmail());

        StepVerifier.create(result)
                .assertNext(profile -> {
                    assertThat(profile.getId()).isEqualTo(testProfile.getId());
                    assertThat(profile.getName()).isEqualTo("Test Profile");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getActiveProfile - Should throw exception when user not found")
    void getActiveProfile_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        Mono<Profile> result = historyService.getActiveProfile("notfound@test.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getActiveProfile - Should throw exception when no profiles found")
    void getActiveProfile_NoProfiles() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.empty());

        Mono<Profile> result = historyService.getActiveProfile(testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getHistory - Should return watch history for user")
    void getHistory_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findByProfileIdOrderByWatchedAtDesc(testProfile.getId())).thenReturn(Flux.just(testHistory));

        Flux<WatchHistoryResponse> result = historyService.getHistory(testUser.getEmail());

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getId()).isEqualTo(testHistory.getId());
                    assertThat(history.getProgressSec()).isEqualTo(120);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getHistory - Should return empty when no history")
    void getHistory_Empty() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findByProfileIdOrderByWatchedAtDesc(testProfile.getId())).thenReturn(Flux.empty());

        Flux<WatchHistoryResponse> result = historyService.getHistory(testUser.getEmail());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("startTracking - Should create new history entry")
    void startTracking_NewEntry() {
        WatchHistoryRequest request = WatchHistoryRequest.builder()
                .episodeId(UUID.randomUUID())
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findByProfileIdAndEpisodeId(testProfile.getId(), request.getEpisodeId())).thenReturn(Mono.empty());

        WatchHistory newHistory = WatchHistory.builder()
                .id(UUID.randomUUID())
                .profileId(testProfile.getId())
                .episodeId(request.getEpisodeId())
                .progressSec(0)
                .watchedAt(Instant.now())
                .build();
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(Mono.just(newHistory));

        Mono<WatchHistoryResponse> result = historyService.startTracking(testUser.getEmail(), request);

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getProgressSec()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("startTracking - Should update existing history entry")
    void startTracking_UpdateExisting() {
        WatchHistoryRequest request = WatchHistoryRequest.builder()
                .episodeId(testHistory.getEpisodeId())
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findByProfileIdAndEpisodeId(testProfile.getId(), request.getEpisodeId())).thenReturn(Mono.just(testHistory));

        testHistory.setProgressSec(0);
        testHistory.setWatchedAt(Instant.now());
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(Mono.just(testHistory));

        Mono<WatchHistoryResponse> result = historyService.startTracking(testUser.getEmail(), request);

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getProgressSec()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateProgress - Should update progress successfully")
    void updateProgress_Success() {
        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressSec(300)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(testHistory.getId())).thenReturn(Mono.just(testHistory));

        testHistory.setProgressSec(300);
        testHistory.setWatchedAt(Instant.now());
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(Mono.just(testHistory));

        Mono<WatchHistoryResponse> result = historyService.updateProgress(testUser.getEmail(), testHistory.getId(), request);

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getProgressSec()).isEqualTo(300);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateProgress - Should throw exception when history not found")
    void updateProgress_HistoryNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressSec(300)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<WatchHistoryResponse> result = historyService.updateProgress(testUser.getEmail(), nonExistentId, request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("updateProgress - Should throw exception when profile mismatch")
    void updateProgress_ProfileMismatch() {
        Profile otherProfile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("Other Profile")
                .build();

        WatchHistory otherProfileHistory = WatchHistory.builder()
                .id(UUID.randomUUID())
                .profileId(otherProfile.getId())
                .episodeId(UUID.randomUUID())
                .progressSec(120)
                .watchedAt(Instant.now())
                .build();

        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressSec(300)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(otherProfileHistory.getId())).thenReturn(Mono.just(otherProfileHistory));

        Mono<WatchHistoryResponse> result = historyService.updateProgress(testUser.getEmail(), otherProfileHistory.getId(), request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("markAsCompleted - Should mark history as completed")
    void markAsCompleted_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(testHistory.getId())).thenReturn(Mono.just(testHistory));

        testHistory.setProgressSec(Integer.MAX_VALUE);
        testHistory.setWatchedAt(Instant.now());
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(Mono.just(testHistory));

        Mono<WatchHistoryResponse> result = historyService.markAsCompleted(testUser.getEmail(), testHistory.getId());

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getCompleted()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("markAsCompleted - Should throw exception when history not found")
    void markAsCompleted_HistoryNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<WatchHistoryResponse> result = historyService.markAsCompleted(testUser.getEmail(), nonExistentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getHistoryById - Should return history by ID")
    void getHistoryById_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(testHistory.getId())).thenReturn(Mono.just(testHistory));

        Mono<WatchHistoryResponse> result = historyService.getHistoryById(testUser.getEmail(), testHistory.getId());

        StepVerifier.create(result)
                .assertNext(history -> {
                    assertThat(history.getId()).isEqualTo(testHistory.getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getHistoryById - Should throw exception when history not found")
    void getHistoryById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(profileRepository.findByUserId(testUser.getId())).thenReturn(Flux.just(testProfile));
        when(watchHistoryRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<WatchHistoryResponse> result = historyService.getHistoryById(testUser.getEmail(), nonExistentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }
}
