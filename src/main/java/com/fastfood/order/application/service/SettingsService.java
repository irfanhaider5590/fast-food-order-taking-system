package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.SettingsRequest;
import com.fastfood.order.application.dto.SettingsResponse;
import com.fastfood.order.domain.entity.Settings;
import com.fastfood.order.domain.entity.Shop;
import com.fastfood.order.domain.entity.User;
import com.fastfood.order.infrastructure.config.CacheConfig;
import com.fastfood.order.infrastructure.repository.SettingsRepository;
import com.fastfood.order.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {
    
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final ShopContextService shopContextService;
    
    @Transactional
    @CacheEvict(value = CacheConfig.SETTINGS, allEntries = true)
    public SettingsResponse createOrUpdateSettings(SettingsRequest request, Long currentUserId) {
        log.info("Creating or updating settings");
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        Shop shop = shopContextService.requireCurrentShop();
        
        Settings settings = settingsRepository.findFirstByShopIdOrderByIdAsc(shop.getId())
                .orElse(Settings.builder()
                        .shop(shop)
                        .createdBy(currentUser)
                        .build());
        
        settings.setShop(shop);
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
    @Cacheable(value = CacheConfig.SETTINGS, key = "'shop-' + @shopContextService.requireCurrentShopId()")
    public SettingsResponse getSettings() {
        log.info("Fetching settings");
        try {
            Long shopId = shopContextService.requireCurrentShopId();
            java.util.Optional<Settings> optionalSettings = settingsRepository.findFirstByShopIdOrderByIdAsc(shopId);
            if (optionalSettings.isPresent()) {
                Settings settings = optionalSettings.get();
                log.info("Found existing settings with ID: {}, brandName: {}", settings.getId(), settings.getBrandName());
                return mapToSettingsResponse(settings);
            } else {
                log.info("No settings found in database, returning default settings");
                return SettingsResponse.builder()
                        .brandName("Order System")
                        .brandLogoUrl(null)
                        .contactPhone(null)
                        .contactEmail(null)
                        .address(null)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Error fetching settings, returning default: {}", e.getMessage());
            return SettingsResponse.builder()
                    .brandName("Order System")
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

