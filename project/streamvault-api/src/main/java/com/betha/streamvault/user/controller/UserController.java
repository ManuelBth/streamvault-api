package com.betha.streamvault.user.controller;

import com.betha.streamvault.auth.filter.JwtAuthenticationWebFilter;
import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Mono<String> getAuthenticatedUsername(HttpServletRequest request) {
        String email = request.getHeader(JwtAuthenticationWebFilter.CURRENT_USER_ATTR);
        if (email != null && !email.isEmpty()) {
            return Mono.just(email);
        }
        return Mono.empty();
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(HttpServletRequest request) {
        return getAuthenticatedUsername(request)
                .flatMap(userService::getCurrentUser)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).build()));
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<UserResponse>> updateCurrentUser(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        return getAuthenticatedUsername(request)
                .flatMap(username -> userService.updateUser(username, updateRequest))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).build()));
    }

    @PutMapping("/me/password")
    public Mono<ResponseEntity<Void>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        return getAuthenticatedUsername(request)
                .flatMap(username -> userService.changePassword(username, changeRequest))
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(
            @PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok);
    }
}
