package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.AddOnRequest;
import com.fastfood.order.application.dto.AddOnResponse;
import com.fastfood.order.application.service.AddOnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/add-ons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnService addOnService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddOnResponse> createAddOn(@Valid @RequestBody AddOnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addOnService.createAddOn(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddOnResponse> updateAddOn(
            @PathVariable Long id,
            @Valid @RequestBody AddOnRequest request) {
        return ResponseEntity.ok(addOnService.updateAddOn(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddOnResponse> getAddOnById(@PathVariable Long id) {
        return ResponseEntity.ok(addOnService.getAddOnById(id));
    }

    @GetMapping
    public ResponseEntity<List<AddOnResponse>> getAllAddOns(
            @RequestParam(required = false) Boolean available) {
        if (available != null && available) {
            return ResponseEntity.ok(addOnService.getAvailableAddOns());
        }
        return ResponseEntity.ok(addOnService.getAllAddOns());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAddOn(@PathVariable Long id) {
        addOnService.deleteAddOn(id);
        return ResponseEntity.noContent().build();
    }
}

