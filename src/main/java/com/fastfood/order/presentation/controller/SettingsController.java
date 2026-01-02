package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.SettingsRequest;
import com.fastfood.order.application.dto.SettingsResponse;
import com.fastfood.order.application.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final SettingsService settingsService;
    
    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(Authentication authentication) {
        log.info("GET /api/settings - Fetching settings");
        try {
            SettingsResponse settings = settingsService.getSettings();
            log.info("GET /api/settings - Returning settings: brandName={}, id={}", 
                    settings.getBrandName(), settings.getId());
            log.debug("GET /api/settings - Full settings response: {}", settings);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("GET /api/settings - Error fetching settings", e);
            throw e;
        }
    }
    
    @PostMapping
    public ResponseEntity<SettingsResponse> createOrUpdateSettings(
            @Valid @RequestBody SettingsRequest request,
            Authentication authentication) {
        log.info("POST /api/settings - Creating or updating settings");
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            SettingsResponse response = settingsService.createOrUpdateSettings(request, userId);
            log.info("POST /api/settings - Settings saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /api/settings - Error saving settings", e);
            throw e;
        }
    }
    
    @PutMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @Valid @RequestBody SettingsRequest request,
            Authentication authentication) {
        log.info("PUT /api/settings - Updating settings");
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            SettingsResponse response = settingsService.createOrUpdateSettings(request, userId);
            log.info("PUT /api/settings - Settings updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PUT /api/settings - Error updating settings", e);
            throw e;
        }
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // For now, return a default user ID. In production, extract from JWT token
            return 1L;
        }
        return 1L;
    }
}

