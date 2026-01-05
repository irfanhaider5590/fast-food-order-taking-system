package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.LicenseRequest;
import com.fastfood.order.application.dto.LicenseResponse;
import com.fastfood.order.application.service.LicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/status")
    public ResponseEntity<LicenseResponse> getLicenseStatus() {
        log.info("GET /api/license/status - Getting license status");
        LicenseService.LicenseStatus status = licenseService.getLicenseStatus();
        String machineId = licenseService.getMachineId();
        
        LicenseResponse response = LicenseResponse.builder()
                .isActivated(status.isActivated())
                .isValid(status.isValid())
                .licenseType(status.getLicenseType())
                .activatedAt(status.getActivatedAt())
                .expiresAt(status.getExpiresAt())
                .daysRemaining(status.getDaysRemaining())
                .message(status.getMessage())
                .machineId(machineId)
                .shouldShowWarning(status.isShouldShowWarning())
                .warningMessage(status.getWarningMessage())
                .build();
        
        log.info("Returning LicenseResponse - isValid: {}, isActivated: {}, daysRemaining: {}", 
                response.isValid(), response.isActivated(), response.getDaysRemaining());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<LicenseResponse> activateLicense(@Valid @RequestBody LicenseRequest request) {
        log.info("POST /api/license/activate - Activating license");
        try {
            licenseService.activateSystem(request.getLicenseKey());
            LicenseService.LicenseStatus status = licenseService.getLicenseStatus();
            String machineId = licenseService.getMachineId();
            
            LicenseResponse response = LicenseResponse.builder()
                    .isActivated(status.isActivated())
                    .isValid(status.isValid())
                    .licenseType(status.getLicenseType())
                    .activatedAt(status.getActivatedAt())
                    .expiresAt(status.getExpiresAt())
                    .daysRemaining(status.getDaysRemaining())
                    .message(status.getMessage())
                    .machineId(machineId)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating license", e);
            throw new RuntimeException("Failed to activate license: " + e.getMessage());
        }
    }

    @GetMapping("/machine-id")
    public ResponseEntity<String> getMachineId() {
        log.info("GET /api/license/machine-id - Getting machine ID");
        return ResponseEntity.ok(licenseService.getMachineId());
    }
}

