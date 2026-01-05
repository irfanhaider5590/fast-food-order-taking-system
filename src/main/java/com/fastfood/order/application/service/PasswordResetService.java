package com.fastfood.order.application.service;

import com.fastfood.order.domain.entity.User;
import com.fastfood.order.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password.reset.admin-secret-key:EMERGENCY_ADMIN_RESET_KEY}")
    private String adminSecretKey;

    /**
     * Reset password for a user by username
     */
    @Transactional
    public void resetPasswordByUsername(String username, String newPassword, String resetBy) {
        log.info("Resetting password for username: {} by: {}", username, resetBy);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", username);
    }

    /**
     * Reset password for a user by user ID
     */
    @Transactional
    public void resetPasswordById(Long userId, String newPassword, String resetBy) {
        log.info("Resetting password for user ID: {} by: {}", userId, resetBy);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password reset successfully for user ID: {}", userId);
    }

    /**
     * Emergency admin password reset (requires secret key)
     */
    @Transactional
    public void resetAdminPassword(String newPassword, String providedSecretKey) {
        log.warn("Emergency admin password reset requested");
        
        // Verify secret key
        if (providedSecretKey == null || !providedSecretKey.equals(adminSecretKey)) {
            throw new RuntimeException("Invalid admin secret key");
        }
        
        // Find admin user (assuming username is "admin")
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
        
        // Update password
        adminUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(adminUser);
        
        log.warn("Emergency admin password reset completed successfully");
    }
}

