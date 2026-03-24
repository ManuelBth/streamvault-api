package com.betha.streamvault.auth.service;

import com.betha.streamvault.auth.dto.RegisterRequest;
import com.betha.streamvault.auth.dto.TokenResponse;
import com.betha.streamvault.auth.model.RefreshToken;
import com.betha.streamvault.auth.repository.RefreshTokenJpaRepository;
import com.betha.streamvault.notification.service.MailUserService;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.UserJpaRepository;
import com.betha.streamvault.shared.exception.EmailAlreadyExistsException;
import com.betha.streamvault.shared.exception.InvalidCredentialsException;
import com.betha.streamvault.shared.exception.InvalidTokenException;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.shared.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailUserService mailUserService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(UserRole.ROLE_USER)
                .isVerified(false)
                .build();
        
        User savedUser = userJpaRepository.save(user);
        
        try {
            mailUserService.createMailAccount(savedUser.getEmail(), request.getPassword());
        } catch (Exception e) {
            log.warn("Could not create mail account for {}: {}", savedUser.getEmail(), e.getMessage());
        }
        
        return generateTokens(savedUser);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(String email, String password) {
        log.info("Login attempt for: {}", email);
        
        User user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        return generateTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        log.info("Refresh token attempt");
        
        String tokenHash = hashToken(refreshToken);
        
        RefreshToken token = refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        
        if (token.getRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Refresh token expired or revoked");
        }
        
        User user = token.getUser();
        
        token.setRevoked(true);
        refreshTokenJpaRepository.save(token);
        
        return generateTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout attempt");
        
        if (refreshToken == null) {
            return;
        }
        
        String tokenHash = hashToken(refreshToken);
        
        refreshTokenJpaRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenJpaRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void confirmEmail(String token) {
        throw new UnsupportedOperationException("confirmEmail not implemented yet");
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
        
        refreshTokenJpaRepository.save(refreshTokenEntity);
        
        return TokenResponse.of(accessToken, refreshToken, 900000);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
