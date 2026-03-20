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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public Mono<Profile> getActiveProfile(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado")))
                .flatMap(user -> profileRepository.findByUserId(user.getId())
                        .collectList()
                        .flatMap(profiles -> {
                            if (profiles.isEmpty()) {
                                return Mono.error(new ResourceNotFoundException("No se encontraron perfiles para el usuario"));
                            }
                            // Use first profile (isKids column was removed in V2 migration)
                            return Mono.just(profiles.get(0));
                        }));
    }

    public Mono<Profile> getActiveProfileById(UUID profileId, String userEmail) {
        return getActiveProfile(userEmail)
                .flatMap(profile -> {
                    if (!profile.getId().equals(profileId)) {
                        return Mono.error(new ResourceNotFoundException("Perfil no encontrado"));
                    }
                    return Mono.just(profile);
                });
    }

    public Flux<WatchHistoryResponse> getHistory(String userEmail) {
        return getActiveProfile(userEmail)
                .flatMapMany(profile -> watchHistoryRepository.findByProfileIdOrderByWatchedAtDesc(profile.getId()))
                .map(this::toResponse);
    }

    public Mono<WatchHistoryResponse> startTracking(String userEmail, WatchHistoryRequest request) {
        return getActiveProfile(userEmail)
                .flatMap(profile -> watchHistoryRepository.findByProfileIdAndEpisodeId(profile.getId(), request.getEpisodeId())
                        .flatMap(existing -> {
                            existing.setProgressSec(0);
                            existing.setWatchedAt(Instant.now());
                            return watchHistoryRepository.save(existing);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            WatchHistory newHistory = WatchHistory.builder()
                                    .profileId(profile.getId())
                                    .episodeId(request.getEpisodeId())
                                    .progressSec(0)
                                    .watchedAt(Instant.now())
                                    .build();
                            return watchHistoryRepository.save(newHistory);
                        })))
                .map(this::toResponse)
                .doOnSuccess(h -> log.info("Started tracking episode {} for profile {}", request.getEpisodeId(), userEmail));
    }

    public Mono<WatchHistoryResponse> updateProgress(String userEmail, UUID historyId, ProgressUpdateRequest request) {
        return getActiveProfile(userEmail)
                .flatMap(profile -> watchHistoryRepository.findById(historyId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Historial no encontrado")))
                        .flatMap(history -> {
                            if (!history.getProfileId().equals(profile.getId())) {
                                return Mono.error(new ResourceNotFoundException("Historial no encontrado"));
                            }
                            history.setProgressSec(request.getProgressSec());
                            history.setWatchedAt(Instant.now());
                            return watchHistoryRepository.save(history);
                        }))
                .map(this::toResponse)
                .doOnSuccess(h -> log.info("Updated progress to {} sec for history {}", request.getProgressSec(), historyId));
    }

    public Mono<WatchHistoryResponse> markAsCompleted(String userEmail, UUID historyId) {
        return getActiveProfile(userEmail)
                .flatMap(profile -> watchHistoryRepository.findById(historyId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Historial no encontrado")))
                        .flatMap(history -> {
                            if (!history.getProfileId().equals(profile.getId())) {
                                return Mono.error(new ResourceNotFoundException("Historial no encontrado"));
                            }
                            history.setProgressSec(Integer.MAX_VALUE);
                            history.setWatchedAt(Instant.now());
                            return watchHistoryRepository.save(history);
                        }))
                .map(this::toResponse)
                .doOnSuccess(h -> log.info("Marked history {} as completed", historyId));
    }

    public Mono<WatchHistoryResponse> getHistoryById(String userEmail, UUID historyId) {
        return getActiveProfile(userEmail)
                .flatMap(profile -> watchHistoryRepository.findById(historyId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Historial no encontrado")))
                        .flatMap(history -> {
                            if (!history.getProfileId().equals(profile.getId())) {
                                return Mono.error(new ResourceNotFoundException("Historial no encontrado"));
                            }
                            return Mono.just(history);
                        }))
                .map(this::toResponse);
    }

    private WatchHistoryResponse toResponse(WatchHistory history) {
        return WatchHistoryResponse.builder()
                .id(history.getId())
                .profileId(history.getProfileId())
                .episodeId(history.getEpisodeId())
                .progressSec(history.getProgressSec())
                .completed(history.getProgressSec() != null && history.getProgressSec() >= Integer.MAX_VALUE / 2)
                .watchedAt(history.getWatchedAt() != null
                        ? history.getWatchedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}
