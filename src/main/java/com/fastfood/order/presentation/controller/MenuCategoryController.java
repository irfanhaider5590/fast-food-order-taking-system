package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.MenuCategoryRequest;
import com.fastfood.order.application.dto.MenuCategoryResponse;
import com.fastfood.order.application.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for menu category operations
 */
@Slf4j
@RestController
@RequestMapping("/api/menu/categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> createCategory(@Valid @RequestBody MenuCategoryRequest request) {
        log.info("POST /api/menu/categories - Creating category: {}", request.getNameEn());
        MenuCategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MenuCategoryRequest request) {
        log.info("PUT /api/menu/categories/{} - Updating category", id);
        MenuCategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategoryResponse> getCategoryById(@PathVariable Long id) {
        log.debug("GET /api/menu/categories/{} - Fetching category", id);
        MenuCategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuCategoryResponse>> getAllCategories(
            @RequestParam(required = false) Boolean active) {
        log.debug("GET /api/menu/categories - active={}", active);
        List<MenuCategoryResponse> categories = categoryService.getAllCategories(active);
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/menu/categories/{} - Deleting category", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

