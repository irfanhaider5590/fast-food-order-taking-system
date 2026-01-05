package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.service.LicenseGenerationService;
import com.fastfood.order.domain.entity.License;
import com.fastfood.order.infrastructure.repository.LicenseRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/licenses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLicenseController {

    private final LicenseGenerationService licenseGenerationService;
    private final LicenseRepository licenseRepository;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateLicense(@RequestBody GenerateLicenseRequest request) {
        log.info("POST /api/admin/licenses/generate - Generating license: {}", request.getLicenseType());
        
        License license = licenseGenerationService.createLicense(
                License.LicenseType.valueOf(request.getLicenseType()),
                request.getClientName(),
                request.getClientEmail(),
                request.getNotes()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("licenseKey", license.getLicenseKey());
        response.put("licenseType", license.getLicenseType().name());
        response.put("durationDays", license.getDurationDays());
        response.put("clientName", license.getClientName());
        response.put("clientEmail", license.getClientEmail());
        response.put("message", "License generated successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LicenseInfo>> getAllLicenses() {
        log.info("GET /api/admin/licenses - Fetching all licenses");
        List<License> licenses = licenseRepository.findAll();
        
        List<LicenseInfo> licenseInfos = licenses.stream().map(license -> {
            LicenseInfo info = new LicenseInfo();
            info.setId(license.getId());
            info.setLicenseKey(license.getLicenseKey());
            info.setLicenseType(license.getLicenseType().name());
            info.setDurationDays(license.getDurationDays());
            info.setActivatedAt(license.getActivatedAt());
            info.setExpiresAt(license.getExpiresAt());
            info.setIsActive(license.getIsActive());
            info.setMachineId(license.getMachineId());
            info.setClientName(license.getClientName());
            info.setClientEmail(license.getClientEmail());
            info.setNotes(license.getNotes());
            info.setIsValid(license.isValid());
            info.setIsExpired(license.isExpired());
            return info;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(licenseInfos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicenseInfo> getLicense(@PathVariable Long id) {
        log.info("GET /api/admin/licenses/{} - Fetching license", id);
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("License not found"));
        
        LicenseInfo info = new LicenseInfo();
        info.setId(license.getId());
        info.setLicenseKey(license.getLicenseKey());
        info.setLicenseType(license.getLicenseType().name());
        info.setDurationDays(license.getDurationDays());
        info.setActivatedAt(license.getActivatedAt());
        info.setExpiresAt(license.getExpiresAt());
        info.setIsActive(license.getIsActive());
        info.setMachineId(license.getMachineId());
        info.setClientName(license.getClientName());
        info.setClientEmail(license.getClientEmail());
        info.setNotes(license.getNotes());
        info.setIsValid(license.isValid());
        info.setIsExpired(license.isExpired());
        
        return ResponseEntity.ok(info);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateLicense(@PathVariable Long id) {
        log.info("POST /api/admin/licenses/{}/deactivate - Deactivating license", id);
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("License not found"));
        
        license.setIsActive(false);
        licenseRepository.save(license);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateLicense(@PathVariable Long id) {
        log.info("POST /api/admin/licenses/{}/activate - Activating license", id);
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("License not found"));
        
        license.setIsActive(true);
        licenseRepository.save(license);
        
        return ResponseEntity.ok().build();
    }

    @Data
    public static class GenerateLicenseRequest {
        private String licenseType; // TRIAL, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
        private String clientName;
        private String clientEmail;
        private String notes;
    }

    @Data
    public static class LicenseInfo {
        private Long id;
        private String licenseKey;
        private String licenseType;
        private Integer durationDays;
        private java.time.LocalDateTime activatedAt;
        private java.time.LocalDateTime expiresAt;
        private Boolean isActive;
        private String machineId;
        private String clientName;
        private String clientEmail;
        private String notes;
        private Boolean isValid;
        private Boolean isExpired;
    }
}

