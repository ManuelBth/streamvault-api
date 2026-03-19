package com.betha.streamvault.auth.service;

import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.model.RefreshToken;
import com.betha.streamvault.auth.repository.RefreshTokenRepository;
import com.betha.streamvault.notification.service.MailUserService;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailUserService mailUserService;

    private AuthService authService;
    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                jwtService,
                passwordEncoder,
                mailUserService
        );

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .role(User.ROLE_USER)
                .isVerified(false)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@streamvault.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
    }

    @Test
    @DisplayName("Register - Should create new user successfully")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(mailUserService.createMailAccount(anyString(), anyString())).thenReturn(Mono.empty());
        when(jwtService.generateAccessToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(Mono.just(RefreshToken.builder().build()));

        // When & Then
        StepVerifier.create(authService.register(registerRequest))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getAccessToken()).isEqualTo("access-token");
                    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
                    assertThat(response.getTokenType()).isEqualTo("Bearer");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Register - Should fail when email already exists")
    void register_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(authService.register(registerRequest))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Email already registered"))
                .verify();
    }

    @Test
    @DisplayName("Login - Should authenticate user successfully")
    void login_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(Mono.just(RefreshToken.builder().build()));

        // When & Then
        StepVerifier.create(authService.login("test@streamvault.com", "password123"))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getAccessToken()).isEqualTo("access-token");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Login - Should fail with invalid credentials")
    void login_InvalidCredentials() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        StepVerifier.create(authService.login("test@streamvault.com", "wrongpassword"))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Invalid credentials"))
                .verify();
    }

    @Test
    @DisplayName("Login - Should fail when user not found")
    void login_UserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(authService.login("notfound@streamvault.com", "password"))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Invalid credentials"))
                .verify();
    }

    @Test
    @DisplayName("Refresh - Should generate new tokens successfully")
    void refresh_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .tokenHash("hashed-token")
                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Mono.just(storedToken));
        when(userRepository.findById(any(UUID.class))).thenReturn(Mono.just(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(Mono.just(storedToken));
        when(jwtService.generateAccessToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any(UUID.class)))
                .thenReturn("new-refresh-token");

        // When & Then
        StepVerifier.create(authService.refresh(refreshToken))
                .assertNext(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
                    assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Refresh - Should fail with expired token")
    void refresh_ExpiredToken() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .tokenHash("hashed-token")
                .expiresAt(java.time.Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Mono.just(expiredToken));

        // When & Then
        StepVerifier.create(authService.refresh("expired-token"))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Refresh token expired or revoked"))
                .verify();
    }

    @Test
    @DisplayName("Refresh - Should fail with revoked token")
    void refresh_RevokedToken() {
        // Given
        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .tokenHash("hashed-token")
                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Mono.just(revokedToken));

        // When & Then
        StepVerifier.create(authService.refresh("revoked-token"))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Refresh token expired or revoked"))
                .verify();
    }

    @Test
    @DisplayName("Logout - Should revoke token successfully")
    void logout_Success() {
        // Given
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Mono.just(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(Mono.just(storedToken));

        // When & Then
        StepVerifier.create(authService.logout("refresh-token"))
                .verifyComplete();
    }
}
