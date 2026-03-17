package com.betha.streamvault.auth.controller;

import com.betha.streamvault.auth.dto.LoginRequest;
import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.service.AuthService;
import com.betha.streamvault.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /register - Should register user successfully")
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@streamvault.local");
        request.setPassword("password123");
        request.setName("Test User");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(Mono.just(response));

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
               .post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token")
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }

    @Test
    @DisplayName("POST /login - Should login user successfully")
    void login_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@streamvault.local");
        request.setPassword("password123");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);
        
        when(authService.login(anyString(), anyString())).thenReturn(Mono.just(response));

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token");
    }

    @Test
    @DisplayName("POST /refresh - Should refresh token successfully")
    void refresh_Success() {
        // Given
        TokenResponse response = TokenResponse.of("new-access-token", "new-refresh-token", 900000);
        
        when(authService.refresh(anyString())).thenReturn(Mono.just(response));

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"valid-token\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("POST /logout - Should logout user successfully")
    void logout_Success() {
        // Given
        when(authService.logout(anyString())).thenReturn(Mono.empty());

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"valid-token\"}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET /confirm - Should confirm email")
    void confirm_Success() {
        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/auth/confirm?token=some-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email confirmed");
    }
}

