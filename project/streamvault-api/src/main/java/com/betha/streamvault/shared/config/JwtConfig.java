package com.betha.streamvault.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.public-key:classpath:keys/jwt-public.pem}")
    private Resource publicKeyResource;

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes());
        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(spec);
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        return ReactiveJwtDecoders.fromIssuerLocation("streamvault");
    }

    @Bean
    public JwtDecoder jwtDecoderSimple(RSAPublicKey rsaPublicKey) {
        return new JwtDecoder() {
            @Override
            public org.springframework.security.oauth2.jwt.Jwt decode(String token) {
                try {
                    var decoder = ReactiveJwtDecoders.fromIssuerLocation("streamvault");
                    return decoder.decode(token).block();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decode JWT", e);
                }
            }
        };
    }
}
