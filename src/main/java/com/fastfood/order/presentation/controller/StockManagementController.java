package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.application.service.StockManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockManagementController {

    private final StockManagementService stockManagementService;

    @GetMapping("/items")
    public ResponseEntity<List<StockItemResponse>> getAllStockItems() {
        log.info("GET /api/stock/items - Fetching all stock items");
        List<StockItemResponse> items = stockManagementService.getAllStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/active")
    public ResponseEntity<List<StockItemResponse>> getActiveStockItems() {
        log.info("GET /api/stock/items/active - Fetching active stock items");
        List<StockItemResponse> items = stockManagementService.getActiveStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemResponse> getStockItemById(@PathVariable Long id) {
        log.info("GET /api/stock/items/{} - Fetching stock item", id);
        StockItemResponse item = stockManagementService.getStockItemById(id);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/items")
    public ResponseEntity<StockItemResponse> createStockItem(
            @Valid @RequestBody StockItemRequest request,
            Authentication authentication) {
        log.info("POST /api/stock/items - Creating stock item: {}", request.getNameEn());
        StockItemResponse response = stockManagementService.createStockItem(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<StockItemResponse> updateStockItem(
            @PathVariable Long id,
            @Valid @RequestBody StockItemRequest request,
            Authentication authentication) {
        log.info("PUT /api/stock/items/{} - Updating stock item", id);
        StockItemResponse response = stockManagementService.updateStockItem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteStockItem(@PathVariable Long id) {
        log.info("DELETE /api/stock/items/{} - Deleting stock item", id);
        stockManagementService.deleteStockItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/menu-items/{menuItemId}/ingredients")
    public ResponseEntity<List<MenuItemIngredientResponse>> getMenuItemIngredients(
            @PathVariable Long menuItemId) {
        log.info("GET /api/stock/menu-items/{}/ingredients - Fetching ingredients", menuItemId);
        List<MenuItemIngredientResponse> ingredients = stockManagementService.getMenuItemIngredients(menuItemId);
        return ResponseEntity.ok(ingredients);
    }

    @PostMapping("/menu-items/{menuItemId}/ingredients")
    public ResponseEntity<Void> saveMenuItemIngredients(
            @PathVariable Long menuItemId,
            @Valid @RequestBody List<MenuItemIngredientRequest> ingredients,
            Authentication authentication) {
        log.info("POST /api/stock/menu-items/{}/ingredients - Saving {} ingredients", menuItemId, ingredients.size());
        stockManagementService.saveMenuItemIngredients(menuItemId, ingredients);
        return ResponseEntity.ok().build();
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
}

