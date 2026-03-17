package com.betha.streamvault.user.controller;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.ProfileService;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping
    public Mono<ResponseEntity<java.util.List<ProfileResponse>>> getProfiles(
            @AuthenticationPrincipal String username) {
        
        return userService.getCurrentUser(username)
                .flatMap(user -> profileService.getProfilesByUserId(user.getId()).collectList())
                .map(profiles -> ResponseEntity.ok(profiles));
    }

    @PostMapping
    public Mono<ResponseEntity<ProfileResponse>> createProfile(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody ProfileRequest request) {
        
        return userService.getCurrentUser(username)
                .flatMap(user -> profileService.createProfile(user.getId(), request))
                .map(profile -> ResponseEntity.status(HttpStatus.CREATED).body(profile));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProfileResponse>> getProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id) {
        
        return userService.getCurrentUser(username)
                .flatMap(user -> profileService.belongsToUser(id, user.getId())
                        .flatMap(belongs -> {
                            if (!belongs) {
                                return Mono.error(new IllegalArgumentException("No autorizado"));
                            }
                            return profileService.getProfileById(id);
                        }))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id,
            @Valid @RequestBody ProfileRequest request) {
        
        return userService.getCurrentUser(username)
                .flatMap(user -> profileService.belongsToUser(id, user.getId())
                        .flatMap(belongs -> {
                            if (!belongs) {
                                return Mono.error(new IllegalArgumentException("No autorizado"));
                            }
                            return profileService.updateProfile(id, request);
                        }))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id) {
        
        return userService.getCurrentUser(username)
                .flatMap(user -> profileService.belongsToUser(id, user.getId())
                        .flatMap(belongs -> {
                            if (!belongs) {
                                return Mono.error(new IllegalArgumentException("No autorizado"));
                            }
                            return profileService.deleteProfile(id);
                        }))
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}
