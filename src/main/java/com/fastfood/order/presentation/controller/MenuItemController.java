package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.MenuItemRequest;
import com.fastfood.order.application.dto.MenuItemResponse;
import com.fastfood.order.application.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for menu item operations
 */
@Slf4j
@RestController
@RequestMapping("/api/menu/items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        log.info("POST /api/menu/items - Creating menu item: {}", request.getNameEn());
        MenuItemResponse response = menuItemService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("PUT /api/menu/items/{} - Updating menu item", id);
        MenuItemResponse response = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        log.debug("GET /api/menu/items/{} - Fetching menu item", id);
        MenuItemResponse response = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems(
            @RequestParam(required = false) Long categoryId) {
        log.debug("GET /api/menu/items - categoryId={}", categoryId);
        List<MenuItemResponse> items = menuItemService.getAllMenuItems(categoryId);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        log.info("DELETE /api/menu/items/{} - Deleting menu item", id);
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}

