package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail = "noreply@streamvault.com";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(SendEmailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        String senderEmail = (request.getFrom() != null && !request.getFrom().isBlank())
                ? request.getFrom()
                : null;
        
        message.setFrom(fromEmail);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        
        if (senderEmail != null) {
            message.setText("Enviado por: " + senderEmail + "\n\n" + request.getBody());
        } else {
            message.setText(request.getBody());
        }
        
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String name) {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(to);
        request.setSubject("Bienvenido a StreamVault");
        request.setBody("Hola " + name + ",\n\n¡Bienvenido a StreamVault! Gracias por registrarte.\n\nSaludos,\nEl equipo de StreamVault");
        
        sendEmail(request);
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(to);
        request.setSubject("Recuperación de contraseña - StreamVault");
        request.setBody("Hola,\n\nHas solicitado recuperar tu contraseña. Usa el siguiente código:\n\n" 
                + resetToken + "\n\nEste código expira en 24 horas.\n\nSi no solicitaste esto, ignora este email.\n\nSaludos,\nEl equipo de StreamVault");
        
        sendEmail(request);
    }

    public void sendNewContentNotification(String to, String contentTitle, String contentType) {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(to);
        request.setSubject("Nuevo contenido disponible: " + contentTitle);
        request.setBody("Hola,\n\n¡Tenemos nuevo contenido disponible en StreamVault!\n\n" 
                + contentTitle + " - " + contentType + "\n\n¡Disfrútalo!\n\nSaludos,\nEl equipo de StreamVault");
        
        sendEmail(request);
    }
}
