package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private SendEmailRequest emailRequest;

    @BeforeEach
    void setUp() {
        emailRequest = new SendEmailRequest();
        emailRequest.setTo("test@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setBody("Test Body");
    }

    @Test
    @DisplayName("sendEmail - Should send email successfully")
    void sendEmail_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        StepVerifier.create(emailService.sendEmail(emailRequest))
                .verifyComplete();

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmail - Should handle mail sender exception")
    void sendEmail_Exception() {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        StepVerifier.create(emailService.sendEmail(emailRequest))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("sendWelcomeEmail - Should send welcome email successfully")
    void sendWelcomeEmail_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        StepVerifier.create(emailService.sendWelcomeEmail("user@example.com", "John Doe"))
                .verifyComplete();

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendPasswordResetEmail - Should send password reset email successfully")
    void sendPasswordResetEmail_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String resetToken = "RESET-123456";

        // When & Then
        StepVerifier.create(emailService.sendPasswordResetEmail("user@example.com", resetToken))
                .verifyComplete();

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendNewContentNotification - Should send new content notification successfully")
    void sendNewContentNotification_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        StepVerifier.create(emailService.sendNewContentNotification(
                "user@example.com",
                "New Movie Title",
                "Movie"
        ))
                .verifyComplete();

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
