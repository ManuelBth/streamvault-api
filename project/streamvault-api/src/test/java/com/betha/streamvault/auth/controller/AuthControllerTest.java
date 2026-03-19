package com.betha.streamvault.auth.controller;

import com.betha.streamvault.auth.dto.LoginRequest;
import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    @DisplayName("POST /register - Should register user successfully")
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@streamvault.com");
        request.setPassword("password123");
        request.setName("Test User");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(Mono.just(response));

        // When
        Mono<ResponseEntity<TokenResponse>> result = authController.register(request);

        // Then
        ResponseEntity<TokenResponse> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("access-token", responseEntity.getBody().getAccessToken());
    }

    @Test
    @DisplayName("POST /login - Should login user successfully")
    void login_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@streamvault.com");
        request.setPassword("password123");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);
        
        when(authService.login(anyString(), anyString())).thenReturn(Mono.just(response));

        // When
        Mono<ResponseEntity<TokenResponse>> result = authController.login(request);

        // Then
        ResponseEntity<TokenResponse> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("access-token", responseEntity.getBody().getAccessToken());
    }

    @Test
    @DisplayName("POST /refresh - Should refresh token successfully")
    void refresh_Success() {
        // Given
        TokenResponse response = TokenResponse.of("new-access-token", "new-refresh-token", 900000);
        
        when(authService.refresh(anyString())).thenReturn(Mono.just(response));

        // When
        Mono<ResponseEntity<TokenResponse>> result = authController.refresh(Map.of("refreshToken", "valid-token"));

        // Then
        ResponseEntity<TokenResponse> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("new-access-token", responseEntity.getBody().getAccessToken());
    }

    @Test
    @DisplayName("POST /logout - Should logout user successfully")
    void logout_Success() {
        // Given
        when(authService.logout(anyString())).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Void>> result = authController.logout(Map.of("refreshToken", "valid-token"));

        // Then
        ResponseEntity<Void> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("GET /confirm - Should confirm email")
    void confirm_Success() {
        // When
        Mono<ResponseEntity<Map<String, String>>> result = authController.confirm("some-token");

        // Then
        ResponseEntity<Map<String, String>> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Email confirmed", responseEntity.getBody().get("message"));
    }
}
