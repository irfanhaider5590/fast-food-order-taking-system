package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.application.service.StockManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for stock management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StockManagementController {

    private final StockManagementService stockManagementService;

    @GetMapping("/items")
    public ResponseEntity<List<StockItemResponse>> getAllStockItems() {
        log.debug("GET /api/stock/items - Fetching all stock items");
        List<StockItemResponse> items = stockManagementService.getAllStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/active")
    public ResponseEntity<List<StockItemResponse>> getActiveStockItems() {
        log.debug("GET /api/stock/items/active - Fetching active stock items");
        List<StockItemResponse> items = stockManagementService.getActiveStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/lookup")
    public ResponseEntity<StockItemResponse> lookupStockItemByBarcode(@RequestParam String barcode) {
        log.debug("GET /api/stock/items/lookup?barcode={}", barcode);
        return ResponseEntity.ok(stockManagementService.findByBarcode(barcode));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemResponse> getStockItemById(@PathVariable Long id) {
        log.debug("GET /api/stock/items/{} - Fetching stock item", id);
        StockItemResponse item = stockManagementService.getStockItemById(id);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockItemResponse> createStockItem(
            @Valid @RequestBody StockItemRequest request,
            Authentication authentication) {
        log.info("POST /api/stock/items - Creating stock item: {}", request.getNameEn());
        StockItemResponse response = stockManagementService.createStockItem(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockItemResponse> updateStockItem(
            @PathVariable Long id,
            @Valid @RequestBody StockItemRequest request,
            Authentication authentication) {
        log.info("PUT /api/stock/items/{} - Updating stock item", id);
        StockItemResponse response = stockManagementService.updateStockItem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStockItem(@PathVariable Long id) {
        log.info("DELETE /api/stock/items/{} - Deleting stock item", id);
        stockManagementService.deleteStockItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/{id}/adjust")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        log.info("POST /api/stock/items/{}/adjust - Adjusting stock by {}", id, quantity);
        stockManagementService.adjustStock(id, quantity, notes);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{id}/quantity")
    public ResponseEntity<StockItemResponse> setStockQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        BigDecimal quantity = new BigDecimal(String.valueOf(body.get("quantity")));
        String notes = body.get("notes") != null ? String.valueOf(body.get("notes")) : null;
        log.info("PUT /api/stock/items/{}/quantity - Setting quantity to {}", id, quantity);
        return ResponseEntity.ok(stockManagementService.setStockQuantity(id, quantity, notes));
    }

    @GetMapping("/items/{id}/consumptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockConsumptionConfigResponse> getStockConsumptions(@PathVariable Long id) {
        log.debug("GET /api/stock/items/{}/consumptions", id);
        return ResponseEntity.ok(stockManagementService.getStockConsumptions(id));
    }

    @PutMapping("/items/{id}/consumptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockConsumptionConfigResponse> saveStockConsumptions(
            @PathVariable Long id,
            @Valid @RequestBody List<StockConsumptionRowRequest> rows,
            Authentication authentication) {
        log.info("PUT /api/stock/items/{}/consumptions - Saving {} rows", id, rows != null ? rows.size() : 0);
        return ResponseEntity.ok(stockManagementService.saveStockConsumptions(id, rows));
    }

    @GetMapping("/consumption-catalog")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsumptionCatalogResponse> getConsumptionCatalog() {
        log.debug("GET /api/stock/consumption-catalog");
        return ResponseEntity.ok(stockManagementService.getConsumptionCatalog());
    }
}
