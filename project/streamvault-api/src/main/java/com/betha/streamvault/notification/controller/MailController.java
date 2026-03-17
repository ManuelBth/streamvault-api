package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/mail")
public class MailController {

    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public Mono<ResponseEntity<Void>> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return emailService.sendEmail(request)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}
