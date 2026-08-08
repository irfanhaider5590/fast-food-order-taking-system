package com.fastfood.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Password utilities — admin-only and only active in the dev profile.
 */
@RestController
@RequestMapping("/api/public/password-utility")
@RequiredArgsConstructor
@Profile("dev")
@PreAuthorize("hasRole('ADMIN')")
public class PasswordUtilityController {

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/generate")
    public Map<String, String> generateHash(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = passwordEncoder.encode(password);

        Map<String, String> response = new HashMap<>();
        response.put("hash", hash);
        return response;
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");
        boolean matches = passwordEncoder.matches(password, hash);

        Map<String, Object> response = new HashMap<>();
        response.put("matches", matches);
        return response;
    }
}
