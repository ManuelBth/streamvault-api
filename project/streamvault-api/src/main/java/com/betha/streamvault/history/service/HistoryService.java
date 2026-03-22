package com.betha.streamvault.history.service;

import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.repository.EpisodeJpaRepository;
import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.model.WatchHistory;
import com.betha.streamvault.history.repository.WatchHistoryJpaRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.ProfileJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class HistoryService {

    private static final int COMPLETED_MARKER = -1;

    private final WatchHistoryJpaRepository watchHistoryJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;
    private final EpisodeJpaRepository episodeJpaRepository;

    @Transactional(readOnly = true)
    public User getActiveUser(String userEmail) {
        return userJpaRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Profile getActiveProfileById(UUID profileId, String userEmail) {
        User user = getActiveUser(userEmail);
        return getProfileById(user.getId(), profileId);
    }

    private Profile getProfileById(UUID userId, UUID profileId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<Profile> profiles = profileJpaRepository.findByUserOrderByCreatedAtDesc(user);
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        return profiles.stream()
                .filter(p -> p.getId().equals(profileId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));
    }

    private UUID resolveProfileId(UUID userId, UUID providedProfileId) {
        if (providedProfileId != null) {
            return providedProfileId;
        }
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<Profile> profiles = profileJpaRepository.findByUserOrderByCreatedAtDesc(user);
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        return profiles.get(0).getId();
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponse> getHistory(String userEmail, UUID profileId) {
        User user = getActiveUser(userEmail);
        UUID resolvedProfileId = resolveProfileId(user.getId(), profileId);
        Profile profile = getProfileById(user.getId(), resolvedProfileId);
        return watchHistoryJpaRepository.findByProfileOrderByWatchedAtDesc(profile).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WatchHistoryResponse startTracking(String userEmail, UUID profileId, WatchHistoryRequest request) {
        User user = getActiveUser(userEmail);
        UUID resolvedProfileId = resolveProfileId(user.getId(), profileId);
        Profile profile = getProfileById(user.getId(), resolvedProfileId);
        Episode episode = episodeJpaRepository.findById(request.getEpisodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Episodio no encontrado"));
        
        WatchHistory history = watchHistoryJpaRepository
                .findByProfileAndEpisode(profile, episode)
                .orElse(null);
        
        if (history != null) {
            history.setProgressSec(0);
            history.setWatchedAt(Instant.now());
        } else {
            history = WatchHistory.builder()
                    .profile(profile)
                    .episode(episode)
                    .progressSec(0)
                    .watchedAt(Instant.now())
                    .build();
        }
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Started tracking episode {} for user {}", request.getEpisodeId(), userEmail);
        return toResponse(saved);
    }

    @Transactional
    public WatchHistoryResponse updateProgress(String userEmail, UUID profileId, UUID historyId, ProgressUpdateRequest request) {
        User user = getActiveUser(userEmail);
        UUID resolvedProfileId = resolveProfileId(user.getId(), profileId);
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfile().getId().equals(resolvedProfileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        history.setProgressSec(request.getProgressSec());
        history.setWatchedAt(Instant.now());
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Updated progress to {} sec for history {}", request.getProgressSec(), historyId);
        return toResponse(saved);
    }

    @Transactional
    public WatchHistoryResponse markAsCompleted(String userEmail, UUID profileId, UUID historyId) {
        User user = getActiveUser(userEmail);
        UUID resolvedProfileId = resolveProfileId(user.getId(), profileId);
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfile().getId().equals(resolvedProfileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        history.setProgressSec(COMPLETED_MARKER);
        history.setWatchedAt(Instant.now());
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Marked history {} as completed", historyId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WatchHistoryResponse getHistoryById(String userEmail, UUID profileId, UUID historyId) {
        User user = getActiveUser(userEmail);
        UUID resolvedProfileId = resolveProfileId(user.getId(), profileId);
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfile().getId().equals(resolvedProfileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        return toResponse(history);
    }

    private WatchHistoryResponse toResponse(WatchHistory history) {
        return WatchHistoryResponse.builder()
                .id(history.getId())
                .profileId(history.getProfile().getId())
                .episodeId(history.getEpisode().getId())
                .progressSec(history.getProgressSec())
                .completed(history.getProgressSec() != null && history.getProgressSec() == COMPLETED_MARKER)
                .watchedAt(history.getWatchedAt() != null
                        ? history.getWatchedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}