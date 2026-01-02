package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.ComboRequest;
import com.fastfood.order.application.dto.ComboResponse;
import com.fastfood.order.application.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComboResponse> createCombo(@Valid @RequestBody ComboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comboService.createCombo(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComboResponse> updateCombo(
            @PathVariable Long id,
            @Valid @RequestBody ComboRequest request) {
        return ResponseEntity.ok(comboService.updateCombo(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComboResponse> getComboById(@PathVariable Long id) {
        return ResponseEntity.ok(comboService.getComboById(id));
    }

    @GetMapping
    public ResponseEntity<List<ComboResponse>> getAllCombos(
            @RequestParam(required = false) Boolean available) {
        if (available != null && available) {
            return ResponseEntity.ok(comboService.getAvailableCombos());
        }
        return ResponseEntity.ok(comboService.getAllCombos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return ResponseEntity.noContent().build();
    }
}

