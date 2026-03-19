package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.service.EmailService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailController Tests")
class MailControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MailController mailController;

    private SendEmailRequest emailRequest;
    private UserResponse testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String testEmail = "test@streamvault.local";

    @BeforeEach
    void setUp() {
        emailRequest = new SendEmailRequest();
        emailRequest.setTo("recipient@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setBody("Test Body Content");

        testUser = UserResponse.builder()
                .id(testUserId)
                .email(testEmail)
                .name("Test User")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("sendEmail - Should send email with user as sender")
    void sendEmail_Success() {
        // Given
        when(userService.getUserById(any(UUID.class))).thenReturn(Mono.just(testUser));
        when(emailService.sendEmail(any(SendEmailRequest.class))).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Void>> result = mailController.sendEmail(emailRequest, testUserId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("sendEmail - Should return OK even when user not found (graceful degradation)")
    void sendEmail_UserNotFound() {
        // Given
        when(userService.getUserById(any(UUID.class))).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Void>> result = mailController.sendEmail(emailRequest, testUserId);

        // Then - returns 200 OK even if user not found (graceful degradation)
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("sendEmail - Should handle error when email service fails")
    void sendEmail_Error() {
        // Given
        when(userService.getUserById(any(UUID.class))).thenReturn(Mono.just(testUser));
        when(emailService.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("SMTP Error")));

        // When
        Mono<ResponseEntity<Void>> result = mailController.sendEmail(emailRequest, testUserId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
