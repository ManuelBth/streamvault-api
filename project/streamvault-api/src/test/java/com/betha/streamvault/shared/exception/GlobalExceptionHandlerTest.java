package com.betha.streamvault.shared.exception;

import com.betha.streamvault.shared.dto.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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

            ResponseEntity<ApiErrorResponse> result = handler.handleInvalidCredentials(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(401);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(401);
            assertThat(result.getBody().getError()).isEqualTo("Unauthorized");
            assertThat(result.getBody().getMessage()).isEqualTo("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("handleEmailAlreadyExistsException")
    class HandleEmailAlreadyExists {

        @Test
        @DisplayName("Should return 409 with ApiErrorResponse")
        void returns409WithApiErrorResponse() {
            EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already registered");

            ResponseEntity<ApiErrorResponse> result = handler.handleEmailAlreadyExists(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(409);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(409);
            assertThat(result.getBody().getError()).isEqualTo("Conflict");
            assertThat(result.getBody().getMessage()).isEqualTo("Email already registered");
        }
    }

    @Nested
    @DisplayName("handleTokenExpiredException")
    class HandleTokenExpired {

        @Test
        @DisplayName("Should return 401 with ApiErrorResponse")
        void returns401WithApiErrorResponse() {
            TokenExpiredException ex = new TokenExpiredException("Refresh token expired or revoked");

            ResponseEntity<ApiErrorResponse> result = handler.handleTokenExpired(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(401);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(401);
            assertThat(result.getBody().getError()).isEqualTo("Unauthorized");
            assertThat(result.getBody().getMessage()).isEqualTo("Refresh token expired or revoked");
        }
    }

    @Nested
    @DisplayName("handleInvalidTokenException")
    class HandleInvalidToken {

        @Test
        @DisplayName("Should return 401 with ApiErrorResponse")
        void returns401WithApiErrorResponse() {
            InvalidTokenException ex = new InvalidTokenException("Invalid refresh token");

            ResponseEntity<ApiErrorResponse> result = handler.handleInvalidToken(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(401);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(401);
            assertThat(result.getBody().getError()).isEqualTo("Unauthorized");
            assertThat(result.getBody().getMessage()).isEqualTo("Invalid refresh token");
        }
    }

    @Nested
    @DisplayName("handleResourceNotFoundException")
    class HandleResourceNotFound {

        @Test
        @DisplayName("Should return 404 with ApiErrorResponse")
        void returns404WithApiErrorResponse() {
            ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

            ResponseEntity<ApiErrorResponse> result = handler.handleResourceNotFound(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(404);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(404);
            assertThat(result.getBody().getError()).isEqualTo("Not Found");
            assertThat(result.getBody().getMessage()).isEqualTo("User not found");
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException")
    class HandleIllegalArgument {

        @Test
        @DisplayName("Should return 400 with ApiErrorResponse")
        void returns400WithApiErrorResponse() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

            ResponseEntity<ApiErrorResponse> result = handler.handleIllegalArgument(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(400);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(400);
            assertThat(result.getBody().getError()).isEqualTo("Bad Request");
            assertThat(result.getBody().getMessage()).isEqualTo("Invalid argument");
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class HandleGenericException {

        @Test
        @DisplayName("Should return 500 with ApiErrorResponse")
        void returns500WithApiErrorResponse() {
            Exception ex = new RuntimeException("Something went wrong");

            ResponseEntity<ApiErrorResponse> result = handler.handleGenericException(ex);

            assertThat(result.getStatusCode().value()).isEqualTo(500);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getStatus()).isEqualTo(500);
            assertThat(result.getBody().getError()).isEqualTo("Internal Server Error");
            assertThat(result.getBody().getMessage()).isEqualTo("Internal server error");
        }
    }
}
