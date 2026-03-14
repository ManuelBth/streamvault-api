package com.betha.streamvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        ReactiveManagementWebSecurityAutoConfiguration.class
})
public class StreamVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamVaultApplication.class, args);
    }
}
