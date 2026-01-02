package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.SettingsRequest;
import com.fastfood.order.application.dto.SettingsResponse;
import com.fastfood.order.domain.entity.Settings;
import com.fastfood.order.domain.entity.User;
import com.fastfood.order.infrastructure.repository.SettingsRepository;
import com.fastfood.order.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {
    
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public SettingsResponse createOrUpdateSettings(SettingsRequest request, Long currentUserId) {
        log.info("Creating or updating settings");
        
        // Get current user for audit
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Get existing settings or create new
        Settings settings = settingsRepository.findFirstByOrderByIdAsc()
                .orElse(Settings.builder()
                        .createdBy(currentUser)
                        .build());
        
        // Update settings
        settings.setBrandName(request.getBrandName());
        settings.setBrandLogoUrl(request.getBrandLogoUrl());
        settings.setContactPhone(request.getContactPhone());
        settings.setContactEmail(request.getContactEmail());
        settings.setAddress(request.getAddress());
        settings.setUpdatedBy(currentUser);
        
        Settings savedSettings = settingsRepository.save(settings);
        log.info("Settings saved successfully");
        
        return mapToSettingsResponse(savedSettings);
    }
    
    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        log.info("Fetching settings");
        try {
            java.util.Optional<Settings> optionalSettings = settingsRepository.findFirstByOrderByIdAsc();
            if (optionalSettings.isPresent()) {
                Settings settings = optionalSettings.get();
                log.info("Found existing settings with ID: {}, brandName: {}", settings.getId(), settings.getBrandName());
                return mapToSettingsResponse(settings);
            } else {
                log.info("No settings found in database, returning default settings");
                // Return default settings if none exist
                return SettingsResponse.builder()
                        .brandName("Fast Food Order System")
                        .brandLogoUrl(null)
                        .contactPhone(null)
                        .contactEmail(null)
                        .address(null)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Settings table does not exist or error fetching settings, returning default: {}", e.getMessage());
            // Return default settings if table doesn't exist
            return SettingsResponse.builder()
                    .brandName("Fast Food Order System")
                    .brandLogoUrl(null)
                    .contactPhone(null)
                    .contactEmail(null)
                    .address(null)
                    .build();
        }
    }
    
    private SettingsResponse mapToSettingsResponse(Settings settings) {
        return SettingsResponse.builder()
                .id(settings.getId())
                .brandName(settings.getBrandName())
                .brandLogoUrl(settings.getBrandLogoUrl())
                .contactPhone(settings.getContactPhone())
                .contactEmail(settings.getContactEmail())
                .address(settings.getAddress())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .createdByUsername(settings.getCreatedBy() != null ? settings.getCreatedBy().getUsername() : null)
                .updatedByUsername(settings.getUpdatedBy() != null ? settings.getUpdatedBy().getUsername() : null)
                .build();
    }
}

