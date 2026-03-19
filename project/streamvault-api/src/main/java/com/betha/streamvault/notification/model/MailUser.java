package com.betha.streamvault.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mail_users")
public class MailUser {

    @Id
    private Long id;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("maildir")
    private String maildir;

    @Column("quota")
    private Long quota;

    @Column("active")
    private Boolean active;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
