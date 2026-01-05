package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.SalesAnalyticsResponse;
import com.fastfood.order.application.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for analytics operations (Admin only)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sales")
    public ResponseEntity<SalesAnalyticsResponse> getSalesAnalytics(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("GET /api/admin/analytics/sales - branchId={}, startDate={}, endDate={}", 
                branchId, startDate, endDate);
        SalesAnalyticsResponse response = analyticsService.getSalesAnalytics(branchId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}

