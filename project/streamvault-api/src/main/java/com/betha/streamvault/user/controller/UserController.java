package com.betha.streamvault.user.controller;

import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal String username) {
        return userService.getCurrentUser(username)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(username, request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/me/password")
    public Mono<ResponseEntity<Void>> changePassword(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(username, request)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(
            @PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok);
    }
}
