package com.betha.streamvault.auth.controller;

import com.betha.streamvault.auth.dto.LoginRequest;
import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - {}", request.getEmail());
        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(r -> log.info("User registered successfully: {}", request.getEmail()))
                .doOnError(e -> log.error("Registration failed: {}", e.getMessage()));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - {}", request.getEmail());
        return authService.login(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("User logged in successfully: {}", request.getEmail()))
                .doOnError(e -> log.error("Login failed: {}", e.getMessage()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        log.info("POST /api/v1/auth/refresh");
        return authService.refresh(refreshToken)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Token refreshed successfully"))
                .doOnError(e -> log.error("Token refresh failed: {}", e.getMessage()));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        log.info("POST /api/v1/auth/logout");
        return authService.logout(refreshToken)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .doOnSuccess(r -> log.info("User logged out successfully"))
                .doOnError(e -> log.error("Logout failed: {}", e.getMessage()));
    }

    @GetMapping("/confirm")
    public Mono<ResponseEntity<Map<String, String>>> confirm(@RequestParam String token) {
        log.info("GET /api/v1/auth/confirm");
        // TODO: Implement email confirmation
        return Mono.just(ResponseEntity.ok(Map.of("message", "Email confirmed")));
    }
}
