package com.betha.streamvault.history.service;

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

    private final WatchHistoryJpaRepository watchHistoryJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;

    @Transactional(readOnly = true)
    public User getActiveUser(String userEmail) {
        return userJpaRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Profile getActiveProfileById(UUID profileId, String userEmail) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        Profile firstProfile = profiles.get(0);
        if (!firstProfile.getId().equals(profileId)) {
            throw new ResourceNotFoundException("Perfil no encontrado");
        }
        return firstProfile;
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponse> getHistory(String userEmail) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        UUID profileId = profiles.get(0).getId();
        return watchHistoryJpaRepository.findByProfileIdOrderByWatchedAtDesc(profileId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WatchHistoryResponse startTracking(String userEmail, WatchHistoryRequest request) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        UUID profileId = profiles.get(0).getId();
        
        WatchHistory history = watchHistoryJpaRepository
                .findByProfileIdAndEpisodeId(profileId, request.getEpisodeId())
                .orElse(null);
        
        if (history != null) {
            history.setProgressSec(0);
            history.setWatchedAt(Instant.now());
        } else {
            history = WatchHistory.builder()
                    .profileId(profileId)
                    .episodeId(request.getEpisodeId())
                    .progressSec(0)
                    .watchedAt(Instant.now())
                    .build();
        }
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Started tracking episode {} for user {}", request.getEpisodeId(), userEmail);
        return toResponse(saved);
    }

    @Transactional
    public WatchHistoryResponse updateProgress(String userEmail, UUID historyId, ProgressUpdateRequest request) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        UUID profileId = profiles.get(0).getId();
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfileId().equals(profileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        history.setProgressSec(request.getProgressSec());
        history.setWatchedAt(Instant.now());
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Updated progress to {} sec for history {}", request.getProgressSec(), historyId);
        return toResponse(saved);
    }

    @Transactional
    public WatchHistoryResponse markAsCompleted(String userEmail, UUID historyId) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        UUID profileId = profiles.get(0).getId();
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfileId().equals(profileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        history.setProgressSec(Integer.MAX_VALUE);
        history.setWatchedAt(Instant.now());
        
        WatchHistory saved = watchHistoryJpaRepository.save(history);
        log.info("Marked history {} as completed", historyId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WatchHistoryResponse getHistoryById(String userEmail, UUID historyId) {
        User user = getActiveUser(userEmail);
        List<Profile> profiles = profileJpaRepository.findByUserId(user.getId());
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron perfiles para el usuario");
        }
        UUID profileId = profiles.get(0).getId();
        
        WatchHistory history = watchHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado"));
        
        if (!history.getProfileId().equals(profileId)) {
            throw new ResourceNotFoundException("Historial no encontrado");
        }
        
        return toResponse(history);
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
