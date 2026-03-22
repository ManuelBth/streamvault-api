package com.betha.streamvault.auth.service;

import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.model.RefreshToken;
import com.betha.streamvault.auth.repository.RefreshTokenJpaRepository;
import com.betha.streamvault.notification.service.MailUserService;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

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
                userJpaRepository,
                refreshTokenJpaRepository,
                jwtService,
                passwordEncoder,
                mailUserService
        );

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .role(UserRole.ROLE_USER)
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
        when(userJpaRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userJpaRepository.save(any(User.class))).thenReturn(testUser);
        when(mailUserService.createMailAccount(anyString(), anyString())).thenReturn(null);
        when(jwtService.generateAccessToken(any(UUID.class), anyString(), any(UserRole.class)))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(refreshTokenJpaRepository.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder().build());

        TokenResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Login - Should authenticate user successfully")
    void login_Success() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any(UUID.class), anyString(), any(UserRole.class)))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(refreshTokenJpaRepository.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder().build());

        TokenResponse response = authService.login("test@streamvault.com", "password123");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("Logout - Should complete without error")
    void logout_Success() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .revoked(false)
                .build();

        when(refreshTokenJpaRepository.findByTokenHash(anyString()))
                .thenReturn(java.util.Optional.of(storedToken));
        when(refreshTokenJpaRepository.save(any(RefreshToken.class)))
                .thenReturn(storedToken);

        authService.logout("refresh-token");
    }
}
