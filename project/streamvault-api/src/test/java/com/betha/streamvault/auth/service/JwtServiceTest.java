package com.betha.streamvault.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate test RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        // Create JwtService with test keys using a custom approach
        // We'll test with a temporary implementation
    }

    @Test
    @DisplayName("JWT generation and validation should work correctly")
    void jwt_GenerateAndValidate() throws Exception {
        // Given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        RSAPublicKey testPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey testPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        // Create JwtService manually for testing
        JwtService testService = new JwtService(
                "classpath:keys/jwt-private.pem",
                "classpath:keys/jwt-public.pem",
                900000L,
                604800000L
        );
        
        UUID userId = UUID.randomUUID();
        String email = "test@streamvault.local";
        String role = "ROLE_USER";
        
        // When
        String accessToken = testService.generateAccessToken(userId, email, role);
        
        // Then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken.split("\\.")).hasSize(3); // JWT has 3 parts
        
        // Verify token is valid
        assertThat(testService.isTokenValid(accessToken)).isTrue();
        
        // Extract claims
        assertThat(testService.getUserIdFromToken(accessToken)).isEqualTo(userId);
        assertThat(testService.getEmailFromToken(accessToken)).isEqualTo(email);
        assertThat(testService.getRoleFromToken(accessToken)).isEqualTo(role);
    }

    @Test
    @DisplayName("JWT validation should fail with invalid token")
    void jwt_InvalidToken() {
        // Given
        JwtService testService = new JwtService(
                "classpath:keys/jwt-private.pem",
                "classpath:keys/jwt-public.pem",
                900000L,
                604800000L
        );
        
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = testService.isTokenValid(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT should extract user ID correctly")
    void jwt_GetUserIdFromToken() throws Exception {
        // Given
        JwtService testService = new JwtService(
                "classpath:keys/jwt-private.pem",
                "classpath:keys/jwt-public.pem",
                900000L,
                604800000L
        );
        
        UUID userId = UUID.randomUUID();
        String token = testService.generateAccessToken(userId, "test@test.com", "ROLE_USER");
        
        // When
        UUID extractedUserId = testService.getUserIdFromToken(token);
        
        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("JWT should return null for invalid claims")
    void jwt_NullClaimsForInvalidToken() {
        // Given
        JwtService testService = new JwtService(
                "classpath:keys/jwt-private.pem",
                "classpath:keys/jwt-public.pem",
                900000L,
                604800000L
        );
        
        // When
        UUID userId = testService.getUserIdFromToken("invalid.token");
        String email = testService.getEmailFromToken("invalid.token");
        String role = testService.getRoleFromToken("invalid.token");
        
        // Then
        assertThat(userId).isNull();
        assertThat(email).isNull();
        assertThat(role).isNull();
    }
}
