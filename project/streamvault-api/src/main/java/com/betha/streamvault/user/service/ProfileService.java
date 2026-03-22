package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.ProfileJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileJpaRepository profileJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public List<ProfileResponse> getProfilesByUserId(UUID userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return profileJpaRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProfileResponse> getProfileById(UUID profileId) {
        return profileJpaRepository.findById(profileId)
                .map(this::toResponse);
    }

    @Transactional
    public ProfileResponse createProfile(UUID userId, ProfileRequest request) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        long count = profileJpaRepository.countByUser(user);
        if (count >= Profile.MAX_PROFILES_PER_USER) {
            throw new IllegalArgumentException(
                    "Máximo de perfiles permitidos: " + Profile.MAX_PROFILES_PER_USER);
        }
        Profile profile = Profile.builder()
                .user(user)
                .name(request.getName())
                .avatarUrl(null)
                .build();
        Profile saved = profileJpaRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public Optional<ProfileResponse> updateProfile(UUID profileId, ProfileRequest request) {
        return profileJpaRepository.findById(profileId)
                .map(profile -> {
                    profile.setName(request.getName());
                    return profileJpaRepository.save(profile);
                })
                .map(this::toResponse);
    }

    @Transactional
    public void deleteProfile(UUID profileId) {
        profileJpaRepository.deleteById(profileId);
    }

    @Transactional(readOnly = true)
    public boolean belongsToUser(UUID profileId, UUID userId) {
        return profileJpaRepository.findById(profileId)
                .map(profile -> profile.getUser().getId().equals(userId))
                .orElse(false);
    }

    private ProfileResponse toResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .avatarUrl(profile.getAvatarUrl())
                .createdAt(profile.getCreatedAt() != null 
                        ? profile.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() 
                        : null)
                .build();
    }
}
