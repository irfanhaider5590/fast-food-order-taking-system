package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.StockWarningResponse;
import com.fastfood.order.domain.entity.StockItem;
import com.fastfood.order.domain.entity.StockWarning;
import com.fastfood.order.infrastructure.repository.StockItemRepository;
import com.fastfood.order.infrastructure.repository.StockWarningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockWarningService {

    private final StockItemRepository stockItemRepository;
    private final StockWarningRepository stockWarningRepository;
    private final StockWarningConfigService configService;

    /**
     * Check for low stock items and create warnings
     * Runs every hour, but only creates warnings based on configured interval
     */
    @Scheduled(fixedRate = 3600000) // Check every hour
    @Transactional
    public void checkAndCreateStockWarnings() {
        // Check if alerts are enabled
        if (!configService.isAlertsEnabled()) {
            log.debug("Stock alerts are disabled, skipping warning check");
            return;
        }
        
        int intervalHours = configService.getWarningIntervalHours();
        log.info("Checking for low stock items (configured interval: {} hours)...", intervalHours);
        
        List<StockItem> allStockItems = stockItemRepository.findAll();
        
        for (StockItem stockItem : allStockItems) {
            if (!stockItem.getIsActive()) {
                continue;
            }
            
            if (stockItem.isLowStock()) {
                createStockWarning(stockItem, intervalHours);
            }
        }
        
        log.info("Stock warning check completed");
    }

    /**
     * Check for low stock items immediately (called when placing orders)
     */
    @Transactional
    public List<StockWarningResponse> checkStockWarningsOnOrder() {
        // Check if alerts are enabled
        if (!configService.isAlertsEnabled()) {
            log.debug("Stock alerts are disabled, skipping warning check on order");
            return new java.util.ArrayList<>();
        }
        
        List<StockWarningResponse> warnings = new java.util.ArrayList<>();
        List<StockItem> allStockItems = stockItemRepository.findAll();
        
        for (StockItem stockItem : allStockItems) {
            if (!stockItem.getIsActive()) {
                continue;
            }
            
            if (stockItem.isLowStock()) {
                // Create warning if it doesn't exist
                createStockWarning(stockItem, configService.getWarningIntervalHours());
                
                // Add to response
                String messageEn = String.format("Low stock alert: %s has only %.2f %s remaining (threshold: %.2f %s)",
                        stockItem.getNameEn(),
                        stockItem.getCurrentQuantity().doubleValue(),
                        stockItem.getUnit(),
                        stockItem.getMinThreshold().doubleValue(),
                        stockItem.getUnit());
                
                String messageUr = String.format("کم اسٹاک انتباہ: %s میں صرف %.2f %s باقی ہے (حد: %.2f %s)",
                        stockItem.getNameUr() != null && !stockItem.getNameUr().isEmpty() ? stockItem.getNameUr() : stockItem.getNameEn(),
                        stockItem.getCurrentQuantity().doubleValue(),
                        stockItem.getUnit(),
                        stockItem.getMinThreshold().doubleValue(),
                        stockItem.getUnit());
                
                warnings.add(StockWarningResponse.builder()
                        .stockItemId(stockItem.getId())
                        .stockItemNameEn(stockItem.getNameEn())
                        .stockItemNameUr(stockItem.getNameUr())
                        .warningMessageEn(messageEn)
                        .warningMessageUr(messageUr)
                        .currentQuantity(stockItem.getCurrentQuantity())
                        .thresholdQuantity(stockItem.getMinThreshold())
                        .isAcknowledged(false)
                        .build());
            }
        }
        
        return warnings;
    }

    @Transactional
    private void createStockWarning(StockItem stockItem, int intervalHours) {
        // Check if there's already an unacknowledged warning for this item
        List<StockWarning> existingWarnings = stockWarningRepository
                .findByStockItemIdAndIsAcknowledgedFalse(stockItem.getId());
        
        if (!existingWarnings.isEmpty()) {
            // Update existing warning timestamp if it's older than configured interval
            StockWarning existingWarning = existingWarnings.get(0);
            if (existingWarning.getCreatedAt().isBefore(LocalDateTime.now().minusHours(intervalHours))) {
                // Delete old warning and create new one
                stockWarningRepository.delete(existingWarning);
            } else {
                // Warning already exists and is recent, skip
                return;
            }
        }
        
        String messageEn = String.format("Low stock alert: %s has only %.2f %s remaining (threshold: %.2f %s)",
                stockItem.getNameEn(),
                stockItem.getCurrentQuantity().doubleValue(),
                stockItem.getUnit(),
                stockItem.getMinThreshold().doubleValue(),
                stockItem.getUnit());
        
        String messageUr = String.format("کم اسٹاک انتباہ: %s میں صرف %.2f %s باقی ہے (حد: %.2f %s)",
                stockItem.getNameUr() != null && !stockItem.getNameUr().isEmpty() ? stockItem.getNameUr() : stockItem.getNameEn(),
                stockItem.getCurrentQuantity().doubleValue(),
                stockItem.getUnit(),
                stockItem.getMinThreshold().doubleValue(),
                stockItem.getUnit());
        
        StockWarning warning = StockWarning.builder()
                .stockItem(stockItem)
                .warningMessageEn(messageEn)
                .warningMessageUr(messageUr)
                .currentQuantity(stockItem.getCurrentQuantity())
                .thresholdQuantity(stockItem.getMinThreshold())
                .isAcknowledged(false)
                .build();
        
        stockWarningRepository.save(warning);
        log.info("Created stock warning for: {}", stockItem.getNameEn());
    }

    public List<StockWarningResponse> getActiveWarnings() {
        return stockWarningRepository.findByIsAcknowledgedFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToWarningResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acknowledgeWarning(Long warningId, String acknowledgedBy) {
        StockWarning warning = stockWarningRepository.findById(warningId)
                .orElseThrow(() -> new RuntimeException("Stock warning not found with ID: " + warningId));
        
        warning.setIsAcknowledged(true);
        warning.setAcknowledgedAt(LocalDateTime.now());
        warning.setAcknowledgedBy(acknowledgedBy);
        
        stockWarningRepository.save(warning);
        log.info("Acknowledged stock warning ID: {}", warningId);
    }

    @Transactional
    public void acknowledgeAllWarnings(String acknowledgedBy) {
        List<StockWarning> warnings = stockWarningRepository.findByIsAcknowledgedFalseOrderByCreatedAtDesc();
        
        for (StockWarning warning : warnings) {
            warning.setIsAcknowledged(true);
            warning.setAcknowledgedAt(LocalDateTime.now());
            warning.setAcknowledgedBy(acknowledgedBy);
        }
        
        stockWarningRepository.saveAll(warnings);
        log.info("Acknowledged {} stock warnings", warnings.size());
    }

    private StockWarningResponse mapToWarningResponse(StockWarning warning) {
        StockItem stockItem = warning.getStockItem();
        return StockWarningResponse.builder()
                .id(warning.getId())
                .stockItemId(stockItem.getId())
                .stockItemNameEn(stockItem.getNameEn())
                .stockItemNameUr(stockItem.getNameUr())
                .warningMessageEn(warning.getWarningMessageEn())
                .warningMessageUr(warning.getWarningMessageUr())
                .currentQuantity(warning.getCurrentQuantity())
                .thresholdQuantity(warning.getThresholdQuantity())
                .isAcknowledged(warning.getIsAcknowledged())
                .createdAt(warning.getCreatedAt())
                .build();
    }
}

