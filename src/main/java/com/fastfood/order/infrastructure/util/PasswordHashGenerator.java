package com.fastfood.order.infrastructure.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes.
 * Use this to generate correct password hashes for database insertion.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for Admin@123
        String password = "Admin@123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("\nSQL Update Statement:");
        System.out.println("UPDATE users SET password_hash = '" + hash + "' WHERE username = 'admin';");
        
        // Verify the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + (matches ? "SUCCESS" : "FAILED"));
    }
}

