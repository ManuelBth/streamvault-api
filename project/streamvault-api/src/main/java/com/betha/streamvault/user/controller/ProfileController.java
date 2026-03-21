package com.betha.streamvault.user.controller;

import com.betha.streamvault.user.dto.ProfileRequest;
import com.betha.streamvault.user.dto.ProfileResponse;
import com.betha.streamvault.user.service.ProfileService;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getProfiles(
            @AuthenticationPrincipal String username) {
        
        var user = userService.getCurrentUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profileService.getProfilesByUserId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody ProfileRequest request) {
        
        var user = userService.getCurrentUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            ProfileResponse profile = profileService.createProfile(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id) {
        
        var user = userService.getCurrentUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!profileService.belongsToUser(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return profileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id,
            @Valid @RequestBody ProfileRequest request) {
        
        var user = userService.getCurrentUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!profileService.belongsToUser(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return profileService.updateProfile(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id) {
        
        var user = userService.getCurrentUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!profileService.belongsToUser(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deleteProfile(id);
        return ResponseEntity.ok().build();
    }
}
