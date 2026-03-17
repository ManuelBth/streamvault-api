package com.betha.streamvault.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Log4j2
@Service
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${app.jwt.private-key:classpath:keys/jwt-private.pem}") String privateKeyPath,
            @Value("${app.jwt.public-key:classpath:keys/jwt-public.pem}") String publicKeyPath,
            @Value("${app.jwt.access-token-expiration:900000}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration) {
        
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private RSAPrivateKey loadPrivateKey(String keyPath) {
        try {
            if (keyPath.startsWith("classpath:")) {
                String resourcePath = keyPath.replace("classpath:", "");
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    log.warn("JWT private key not found at: {}", resourcePath);
                    return null;
                }
                String key = new String(is.readAllBytes());
                return parsePrivateKey(key);
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not load private key: {}", e.getMessage());
            return null;
        }
    }

    private RSAPublicKey loadPublicKey(String keyPath) {
        try {
            if (keyPath.startsWith("classpath:")) {
                String resourcePath = keyPath.replace("classpath:", "");
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    log.warn("JWT public key not found at: {}", resourcePath);
                    return null;
                }
                String key = new String(is.readAllBytes());
                return parsePublicKey(key);
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not load public key: {}", e.getMessage());
            return null;
        }
    }

    private RSAPrivateKey parsePrivateKey(String key) throws Exception {
        key = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private RSAPublicKey parsePublicKey(String key) throws Exception {
        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(spec);
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .issuer("streamvault")
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .issuer("streamvault")
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = validateToken(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("role", String.class);
    }

    public String extractUsername(String token) {
        return getEmailFromToken(token);
    }

    public <T> T extractClaim(String token, String claim, Class<T> clazz) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get(claim, clazz);
    }
}
