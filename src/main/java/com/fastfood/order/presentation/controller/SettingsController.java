package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.SettingsRequest;
import com.fastfood.order.application.dto.SettingsResponse;
import com.fastfood.order.application.service.SettingsService;
import com.fastfood.order.presentation.helper.ControllerHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for application settings management
 */
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final SettingsService settingsService;
    
    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(Authentication authentication) {
        log.debug("GET /api/settings - Fetching settings");
        SettingsResponse settings = settingsService.getSettings();
        return ResponseEntity.ok(settings);
    }
    
    @PostMapping
    public ResponseEntity<SettingsResponse> createOrUpdateSettings(
            @Valid @RequestBody SettingsRequest request,
            Authentication authentication) {
        log.info("POST /api/settings - Saving settings");
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        SettingsResponse response = settingsService.createOrUpdateSettings(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @Valid @RequestBody SettingsRequest request,
            Authentication authentication) {
        log.info("PUT /api/settings - Updating settings");
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        SettingsResponse response = settingsService.createOrUpdateSettings(request, userId);
        return ResponseEntity.ok(response);
    }
}

