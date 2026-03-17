package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Flux<ProfileResponse> getProfilesByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    public Mono<ProfileResponse> getProfileById(UUID profileId) {
        return profileRepository.findById(profileId)
                .map(this::toResponse);
    }

    public Mono<ProfileResponse> createProfile(UUID userId, ProfileRequest request) {
        return profileRepository.countByUserId(userId)
                .flatMap(count -> {
                    if (count >= Profile.MAX_PROFILES_PER_USER) {
                        return Mono.error(new IllegalArgumentException(
                                "Máximo de perfiles permitidos: " + Profile.MAX_PROFILES_PER_USER));
                    }
                    Profile profile = Profile.builder()
                            .userId(userId)
                            .name(request.getName())
                            .avatarUrl(null)
                            .build();
                    return profileRepository.save(profile);
                })
                .map(this::toResponse);
    }

    public Mono<ProfileResponse> updateProfile(UUID profileId, ProfileRequest request) {
        return profileRepository.findById(profileId)
                .flatMap(profile -> {
                    profile.setName(request.getName());
                    return profileRepository.save(profile);
                })
                .map(this::toResponse);
    }

    public Mono<Void> deleteProfile(UUID profileId) {
        return profileRepository.findById(profileId)
                .flatMap(profile -> profileRepository.deleteById(profileId));
    }

    public Mono<Boolean> belongsToUser(UUID profileId, UUID userId) {
        return profileRepository.findById(profileId)
                .map(profile -> profile.getUserId().equals(userId))
                .defaultIfEmpty(false);
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
