package com.fastfood.order.application.service;

import com.fastfood.order.domain.entity.License;
import com.fastfood.order.infrastructure.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseGenerationService {

    private final LicenseRepository licenseRepository;

    /**
     * Generate a unique license key
     */
    public String generateLicenseKey() {
        String key;
        do {
            // Format: XXXX-XXXX-XXXX-XXXX (16 characters, 4 groups)
            key = generateRandomKey();
        } while (licenseRepository.findByLicenseKey(key).isPresent());
        
        return key;
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluding confusing chars
        
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append("-");
            }
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    /**
     * Create a new license
     */
    @Transactional
    public License createLicense(License.LicenseType licenseType, String clientName, String clientEmail, String notes) {
        String licenseKey = generateLicenseKey();
        
        License license = License.builder()
                .licenseKey(licenseKey)
                .licenseType(licenseType)
                .durationDays(licenseType.getDays())
                .clientName(clientName)
                .clientEmail(clientEmail)
                .notes(notes)
                .isActive(true)
                .build();
        
        License saved = licenseRepository.save(license);
        log.info("Created license: {} for client: {} ({})", licenseKey, clientName, licenseType);
        
        return saved;
    }
}

