package com.fastfood.order.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Generates BCrypt hash for Admin@123 password on application startup.
 * Check the console/logs for the hash to use in database.
 */
@Slf4j
@Component
public class PasswordHashStartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin@123";
        String hash = encoder.encode(password);
        
        log.info("========================================");
        log.info("Password Hash Generator");
        log.info("========================================");
        log.info("Password: {}", password);
        log.info("BCrypt Hash: {}", hash);
        log.info("");
        log.info("SQL Update Statement:");
        log.info("UPDATE users SET password_hash = '{}' WHERE username = 'admin';", hash);
        log.info("========================================");
    }
}

