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
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(emailRequest);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmail - Should propagate exception when mail sender fails")
    void sendEmail_Exception() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.function.Executable executable = () -> emailService.sendEmail(emailRequest);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, executable);
    }

    @Test
    @DisplayName("sendWelcomeEmail - Should send welcome email successfully")
    void sendWelcomeEmail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendWelcomeEmail("user@example.com", "John Doe");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendPasswordResetEmail - Should send password reset email successfully")
    void sendPasswordResetEmail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String resetToken = "RESET-123456";

        emailService.sendPasswordResetEmail("user@example.com", resetToken);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendNewContentNotification - Should send new content notification successfully")
    void sendNewContentNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendNewContentNotification("user@example.com", "New Movie Title", "Movie");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}