package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.service.LicenseGenerationService;
import com.fastfood.order.domain.entity.License;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/license/generate")
@RequiredArgsConstructor
public class LicenseGenerationController {

    private final LicenseGenerationService licenseGenerationService;

    @Value("${app.license.generation.api-key:CHANGE_THIS_SECRET_KEY}")
    private String licenseGenerationApiKey;

    @PostMapping
    public ResponseEntity<Map<String, Object>> generateLicense(
            @RequestHeader(value = "X-License-API-Key", required = false) String apiKey,
            @RequestBody GenerateLicenseRequest request) {
        
        log.info("POST /api/license/generate - License generation request received");
        
        // Validate API key
        if (apiKey == null || apiKey.isEmpty() || !apiKey.equals(licenseGenerationApiKey)) {
            log.warn("Invalid or missing API key for license generation");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized: Invalid API key");
            errorResponse.put("code", "INVALID_API_KEY");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        try {
            License license = licenseGenerationService.createLicense(
                    License.LicenseType.valueOf(request.getLicenseType()),
                    request.getClientName(),
                    request.getClientEmail(),
                    request.getNotes()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("licenseKey", license.getLicenseKey());
            response.put("licenseType", license.getLicenseType().name());
            response.put("durationDays", license.getDurationDays() == Integer.MAX_VALUE ? "LIFETIME" : license.getDurationDays());
            response.put("clientName", license.getClientName());
            response.put("clientEmail", license.getClientEmail());
            response.put("message", "License generated successfully");
            response.put("expiresAt", license.getLicenseType() == License.LicenseType.LIFETIME ? "Never" : "Will be set on activation");

            log.info("License generated successfully: {} for client: {}", license.getLicenseKey(), request.getClientName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid license type: {}", request.getLicenseType());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid license type: " + request.getLicenseType());
            errorResponse.put("code", "INVALID_LICENSE_TYPE");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating license", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate license: " + e.getMessage());
            errorResponse.put("code", "GENERATION_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Data
    public static class GenerateLicenseRequest {
        private String licenseType; // TRIAL, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL, LIFETIME
        private String clientName;
        private String clientEmail;
        private String notes;
    }
}

