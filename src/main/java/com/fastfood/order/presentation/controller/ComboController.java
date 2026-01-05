package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.ComboRequest;
import com.fastfood.order.application.dto.ComboResponse;
import com.fastfood.order.application.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for combo operations
 */
@Slf4j
@RestController
@RequestMapping("/api/menu/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComboResponse> createCombo(@Valid @RequestBody ComboRequest request) {
        log.info("POST /api/menu/combos - Creating combo: {}", request.getNameEn());
        ComboResponse response = comboService.createCombo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComboResponse> updateCombo(
            @PathVariable Long id,
            @Valid @RequestBody ComboRequest request) {
        log.info("PUT /api/menu/combos/{} - Updating combo", id);
        ComboResponse response = comboService.updateCombo(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComboResponse> getComboById(@PathVariable Long id) {
        log.debug("GET /api/menu/combos/{} - Fetching combo", id);
        ComboResponse response = comboService.getComboById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ComboResponse>> getAllCombos(
            @RequestParam(required = false) Boolean available) {
        log.debug("GET /api/menu/combos - available={}", available);
        List<ComboResponse> combos = comboService.getAllCombos(available);
        return ResponseEntity.ok(combos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCombo(@PathVariable Long id) {
        log.info("DELETE /api/menu/combos/{} - Deleting combo", id);
        comboService.deleteCombo(id);
        return ResponseEntity.noContent().build();
    }
}

