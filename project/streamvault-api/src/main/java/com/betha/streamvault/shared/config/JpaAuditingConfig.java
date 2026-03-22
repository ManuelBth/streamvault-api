package com.betha.streamvault.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing configuration.
 * Enables automatic population of @CreatedDate and @LastModifiedDate
 * via BaseEntity subclasses.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
