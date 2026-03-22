package com.betha.streamvault.notification.model;

import com.betha.streamvault.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mail_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "maildir", nullable = false)
    private String maildir;

    @Column(name = "quota")
    private Long quota;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
