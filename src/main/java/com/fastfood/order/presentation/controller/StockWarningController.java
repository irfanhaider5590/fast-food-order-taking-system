package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.StockWarningResponse;
import com.fastfood.order.application.service.StockWarningService;
import com.fastfood.order.application.service.StockWarningConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stock/warnings")
@RequiredArgsConstructor
public class StockWarningController {

    private final StockWarningService stockWarningService;
    private final StockWarningConfigService configService;

    @GetMapping
    public ResponseEntity<List<StockWarningResponse>> getActiveWarnings() {
        log.info("GET /api/stock/warnings - Fetching active stock warnings");
        List<StockWarningResponse> warnings = stockWarningService.getActiveWarnings();
        return ResponseEntity.ok(warnings);
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeWarning(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /api/stock/warnings/{}/acknowledge - Acknowledging warning", id);
        String acknowledgedBy = authentication != null ? authentication.getName() : "System";
        stockWarningService.acknowledgeWarning(id, acknowledgedBy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/acknowledge-all")
    public ResponseEntity<Void> acknowledgeAllWarnings(Authentication authentication) {
        log.info("POST /api/stock/warnings/acknowledge-all - Acknowledging all warnings");
        String acknowledgedBy = authentication != null ? authentication.getName() : "System";
        stockWarningService.acknowledgeAllWarnings(acknowledgedBy);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config/interval")
    public ResponseEntity<Map<String, Object>> getWarningInterval() {
        log.info("GET /api/stock/warnings/config/interval - Getting warning interval");
        Map<String, Object> response = new HashMap<>();
        response.put("intervalHours", configService.getWarningIntervalHours());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/config/interval")
    public ResponseEntity<Void> setWarningInterval(@RequestParam int hours, Authentication authentication) {
        log.info("POST /api/stock/warnings/config/interval - Setting warning interval to {} hours", hours);
        if (hours < 1 || hours > 24) {
            throw new RuntimeException("Warning interval must be between 1 and 24 hours");
        }
        configService.setWarningIntervalHours(hours);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-now")
    public ResponseEntity<List<StockWarningResponse>> checkWarningsNow() {
        log.info("POST /api/stock/warnings/check-now - Manually checking for stock warnings");
        // Trigger immediate check
        stockWarningService.checkAndCreateStockWarnings();
        List<StockWarningResponse> warnings = stockWarningService.getActiveWarnings();
        return ResponseEntity.ok(warnings);
    }

    @GetMapping("/config/alerts-enabled")
    public ResponseEntity<Map<String, Object>> getAlertsEnabled() {
        log.info("GET /api/stock/warnings/config/alerts-enabled - Getting alerts enabled status");
        Map<String, Object> response = new HashMap<>();
        response.put("alertsEnabled", configService.isAlertsEnabled());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/config/alerts-enabled")
    public ResponseEntity<Void> setAlertsEnabled(@RequestParam boolean enabled) {
        log.info("POST /api/stock/warnings/config/alerts-enabled - Setting alerts enabled to {}", enabled);
        configService.setAlertsEnabled(enabled);
        return ResponseEntity.ok().build();
    }
}

