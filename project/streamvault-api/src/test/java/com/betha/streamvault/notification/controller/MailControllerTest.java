package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.service.EmailService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailController Tests")
class MailControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    private MailController mailController;

    private SendEmailRequest emailRequest;
    private UserResponse testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String testEmail = "test@streamvault.com";

    @BeforeEach
    void setUp() {
        mailController = new MailController(emailService, userService);
        
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
        when(userService.getCurrentUser(testEmail)).thenReturn(testUser);
        doNothing().when(emailService).sendEmail(any(SendEmailRequest.class));

        ResponseEntity<Void> response = mailController.sendEmail(emailRequest, testEmail);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(emailService).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("sendEmail - Should return 200 when user not found (graceful degradation)")
    void sendEmail_UserNotFound() {
        when(userService.getCurrentUser(testEmail)).thenReturn(null);

        ResponseEntity<Void> response = mailController.sendEmail(emailRequest, testEmail);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    @DisplayName("sendEmail - Should propagate exception when email service fails")
    void sendEmail_Error() {
        when(userService.getCurrentUser(testEmail)).thenReturn(testUser);
        doThrow(new RuntimeException("SMTP Error")).when(emailService).sendEmail(any(SendEmailRequest.class));

        org.junit.jupiter.api.function.Executable executable = () -> mailController.sendEmail(emailRequest, testEmail);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, executable);
    }
}