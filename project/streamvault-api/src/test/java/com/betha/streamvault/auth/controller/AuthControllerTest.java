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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@streamvault.com");
        request.setPassword("password123");
        request.setName("Test User");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        ResponseEntity<TokenResponse> result = authController.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("POST /login - Should login user successfully")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@streamvault.com");
        request.setPassword("password123");

        TokenResponse response = TokenResponse.of("access-token", "refresh-token", 900000);

        when(authService.login(anyString(), anyString())).thenReturn(response);

        ResponseEntity<TokenResponse> result = authController.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("POST /refresh - Should refresh token successfully")
    void refresh_Success() {
        TokenResponse response = TokenResponse.of("new-access-token", "new-refresh-token", 900000);

        when(authService.refresh(anyString())).thenReturn(response);

        ResponseEntity<TokenResponse> result = authController.refresh(Map.of("refreshToken", "valid-token"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("POST /logout - Should logout user successfully")
    void logout_Success() {
        doNothing().when(authService).logout(anyString());

        ResponseEntity<Void> result = authController.logout(Map.of("refreshToken", "valid-token"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
