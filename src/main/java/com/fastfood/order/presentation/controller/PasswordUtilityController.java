package com.fastfood.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/password-utility")
@RequiredArgsConstructor
public class PasswordUtilityController {

    private final PasswordEncoder passwordEncoder;

    /**
     * Generate BCrypt hash for a password (Development only)
     * POST /api/public/password-utility/generate
     * Body: { "password": "Admin@123" }
     */
    @PostMapping("/generate")
    public Map<String, String> generateHash(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = passwordEncoder.encode(password);
        
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("sql", "UPDATE users SET password_hash = '" + hash + "' WHERE username = 'admin';");
        return response;
    }

    /**
     * Verify if a password matches a hash
     * POST /api/public/password-utility/verify
     * Body: { "password": "Admin@123", "hash": "$2a$10$..." }
     */
    @PostMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");
        boolean matches = passwordEncoder.matches(password, hash);
        
        Map<String, Object> response = new HashMap<>();
        response.put("matches", matches);
        response.put("message", matches ? "Password matches!" : "Password does not match!");
        return response;
    }
}

