package com.betha.streamvault.auth.controller;

import com.betha.streamvault.auth.dto.LoginRequest;
import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - {}", request.getEmail());
        TokenResponse response = authService.register(request);
        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - {}", request.getEmail());
        TokenResponse response = authService.login(request.getEmail(), request.getPassword());
        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        log.info("POST /api/v1/auth/refresh");
        TokenResponse response = authService.refresh(refreshToken);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        log.info("POST /api/v1/auth/logout");
        authService.logout(refreshToken);
        log.info("User logged out successfully");
        return ResponseEntity.noContent().build();
    }
}
