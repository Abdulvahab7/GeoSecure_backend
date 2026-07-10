package com.geosecure.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the BCryptPasswordEncoder bean used both by SecurityConfig's
 * AuthenticationProvider and by the auth service for hashing new passwords.
 * Strength is externalized to application.properties
 * (geosecure.security.bcrypt-strength).
 */
@Configuration
public class PasswordConfig {

    @Value("${geosecure.security.bcrypt-strength:12}")
    private int bcryptStrength;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
