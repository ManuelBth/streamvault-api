package com.betha.streamvault.shared.exception;

import com.betha.streamvault.shared.dto.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Nested
    @DisplayName("handleInvalidCredentialsException")
    class HandleInvalidCredentials {

        @Test
        @DisplayName("Should return 401 with ApiErrorResponse")
        void returns401WithApiErrorResponse() {
            InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleInvalidCredentials(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(401);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(401);
                        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
                        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleEmailAlreadyExistsException")
    class HandleEmailAlreadyExists {

        @Test
        @DisplayName("Should return 409 with ApiErrorResponse")
        void returns409WithApiErrorResponse() {
            EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already registered");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleEmailAlreadyExists(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(409);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(409);
                        assertThat(response.getBody().getError()).isEqualTo("Conflict");
                        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleTokenExpiredException")
    class HandleTokenExpired {

        @Test
        @DisplayName("Should return 401 with ApiErrorResponse")
        void returns401WithApiErrorResponse() {
            TokenExpiredException ex = new TokenExpiredException("Refresh token expired or revoked");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleTokenExpired(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(401);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(401);
                        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
                        assertThat(response.getBody().getMessage()).isEqualTo("Refresh token expired or revoked");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleInvalidTokenException")
    class HandleInvalidToken {

        @Test
        @DisplayName("Should return 401 with ApiErrorResponse")
        void returns401WithApiErrorResponse() {
            InvalidTokenException ex = new InvalidTokenException("Invalid refresh token");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleInvalidToken(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(401);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(401);
                        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
                        assertThat(response.getBody().getMessage()).isEqualTo("Invalid refresh token");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleResourceNotFoundException")
    class HandleResourceNotFound {

        @Test
        @DisplayName("Should return 404 with ApiErrorResponse")
        void returns404WithApiErrorResponse() {
            ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleResourceNotFound(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(404);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(404);
                        assertThat(response.getBody().getError()).isEqualTo("Not Found");
                        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException")
    class HandleIllegalArgument {

        @Test
        @DisplayName("Should return 400 with ApiErrorResponse")
        void returns400WithApiErrorResponse() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleIllegalArgument(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(400);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(400);
                        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
                        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class HandleGenericException {

        @Test
        @DisplayName("Should return 500 with ApiErrorResponse and log error")
        void returns500WithApiErrorResponse() {
            Exception ex = new RuntimeException("Something went wrong");

            Mono<ResponseEntity<ApiErrorResponse>> result = handler.handleGenericException(ex);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode().value()).isEqualTo(500);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getStatus()).isEqualTo(500);
                        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
                        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
                        assertThat(response.getBody().getTimestamp()).isNotNull();
                    })
                    .verifyComplete();
        }
    }
}
