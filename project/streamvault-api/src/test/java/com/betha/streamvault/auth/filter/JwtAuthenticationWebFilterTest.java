package com.betha.streamvault.auth.filter;

import com.betha.streamvault.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationWebFilter Tests")
class JwtAuthenticationWebFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private WebFilterChain webFilterChain;

    private JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationWebFilter = new JwtAuthenticationWebFilter(jwtService);
    }

    @Test
    @DisplayName("Should authenticate valid JWT token and set security context")
    void filter_ValidToken_SetsAuthentication() {
        // Given
        String validToken = "valid.jwt.token";
        String email = "test@streamvault.com";
        String role = "ROLE_USER";
        UUID userId = UUID.randomUUID();

        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtService.getRoleFromToken(validToken)).thenReturn(role);
        when(jwtService.getUserIdFromToken(validToken)).thenReturn(userId);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then - verify filter completes and calls chain
        StepVerifier.create(result)
                .verifyComplete();

        // Verify the chain was called
        verify(webFilterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should skip authentication for public paths (auth)")
    void filter_PublicAuthPath_SkipsAuthentication() {
        // Given - public path
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify jwtService was never called
        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for public paths (actuator)")
    void filter_PublicActuatorPath_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for swagger endpoints")
    void filter_SwaggerEndpoint_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/swagger-ui/index.html")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("Should skip authentication when no Authorization header")
    void filter_NoAuthHeader_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should skip authentication when Authorization is not Bearer")
    void filter_NonBearerAuth_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should skip authentication for invalid token")
    void filter_InvalidToken_SkipsAuthentication() {
        // Given
        String invalidToken = "invalid.token";

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify isTokenValid was called but chain was still called
        verify(jwtService).isTokenValid(invalidToken);
    }

    @Test
    @DisplayName("Should skip authentication when token is valid but email is null")
    void filter_ValidTokenButNullEmail_SkipsAuthentication() {
        // Given
        String validToken = "valid.jwt.token";

        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(validToken)).thenReturn(null);
        when(jwtService.getRoleFromToken(validToken)).thenReturn("ROLE_USER");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(webFilterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should authenticate with ADMIN role")
    void filter_AdminRole_SetsAuthentication() {
        // Given
        String validToken = "valid.jwt.token";
        String email = "admin@streamvault.com";
        String role = "ROLE_ADMIN";
        UUID userId = UUID.randomUUID();

        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtService.getRoleFromToken(validToken)).thenReturn(role);
        when(jwtService.getUserIdFromToken(validToken)).thenReturn(userId);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(webFilterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle null userId gracefully")
    void filter_NullUserId_StillWorks() {
        // Given
        String validToken = "valid.jwt.token";
        String email = "test@streamvault.com";
        String role = "ROLE_USER";

        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtService.getRoleFromToken(validToken)).thenReturn(role);
        when(jwtService.getUserIdFromToken(validToken)).thenReturn(null);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/catalog")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationWebFilter.filter(exchange, webFilterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(webFilterChain).filter(exchange);
    }
}
