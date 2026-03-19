package com.betha.streamvault.auth.service;

import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.model.RefreshToken;
import com.betha.streamvault.auth.repository.RefreshTokenRepository;
import com.betha.streamvault.notification.service.MailUserService;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailUserService mailUserService;

    public Mono<TokenResponse> register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Email already registered"));
                    }
                    
                    User user = User.builder()
                            .email(request.getEmail())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .name(request.getName())
                            .role(User.ROLE_USER)
                            .isVerified(false)
                            .createdAt(Instant.now())
                            .build();
                    
                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                // Create mail account automatically
                                return mailUserService.createMailAccount(savedUser.getEmail(), request.getPassword())
                                        .then(generateTokens(savedUser));
                            });
                });
    }

    public Mono<TokenResponse> login(String email, String password) {
        log.info("Login attempt for: {}", email);
        
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }
                    return generateTokens(user);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }

    public Mono<TokenResponse> refresh(String refreshToken) {
        log.info("Refresh token attempt");
        
        String tokenHash = hashToken(refreshToken);
        
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .flatMap(token -> {
                    if (token.getRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
                        return Mono.error(new RuntimeException("Refresh token expired or revoked"));
                    }
                    
                    return userRepository.findById(token.getUserId())
                            .flatMap(user -> {
                                // Revoke old refresh token
                                token.setRevoked(true);
                                return refreshTokenRepository.save(token)
                                        .flatMap(v -> generateTokens(user));
                            });
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")));
    }

    public Mono<Void> logout(String refreshToken) {
        log.info("Logout attempt");
        
        String tokenHash = hashToken(refreshToken);
        
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .flatMap(token -> {
                    token.setRevoked(true);
                    return refreshTokenRepository.save(token);
                })
                .then();
    }

    public Mono<User> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }

    private Mono<TokenResponse> generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        
        return refreshTokenRepository.save(refreshTokenEntity)
                .thenReturn(TokenResponse.of(accessToken, refreshToken, 900000));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
