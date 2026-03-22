package com.betha.streamvault.user.model;

/**
 * User roles for authorization.
 * Used with @Enumerated(EnumType.STRING) in User entity.
 */
public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN
}
