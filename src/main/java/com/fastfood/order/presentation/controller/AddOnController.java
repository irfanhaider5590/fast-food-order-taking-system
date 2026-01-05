package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.AddOnRequest;
import com.fastfood.order.application.dto.AddOnResponse;
import com.fastfood.order.application.service.AddOnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for add-on operations
 */
@Slf4j
@RestController
@RequestMapping("/api/menu/add-ons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnService addOnService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddOnResponse> createAddOn(@Valid @RequestBody AddOnRequest request) {
        log.info("POST /api/menu/add-ons - Creating add-on: {}", request.getNameEn());
        AddOnResponse response = addOnService.createAddOn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddOnResponse> updateAddOn(
            @PathVariable Long id,
            @Valid @RequestBody AddOnRequest request) {
        log.info("PUT /api/menu/add-ons/{} - Updating add-on", id);
        AddOnResponse response = addOnService.updateAddOn(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddOnResponse> getAddOnById(@PathVariable Long id) {
        log.debug("GET /api/menu/add-ons/{} - Fetching add-on", id);
        AddOnResponse response = addOnService.getAddOnById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AddOnResponse>> getAllAddOns(
            @RequestParam(required = false) Boolean available) {
        log.debug("GET /api/menu/add-ons - available={}", available);
        List<AddOnResponse> addOns = addOnService.getAllAddOns(available);
        return ResponseEntity.ok(addOns);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAddOn(@PathVariable Long id) {
        log.info("DELETE /api/menu/add-ons/{} - Deleting add-on", id);
        addOnService.deleteAddOn(id);
        return ResponseEntity.noContent().build();
    }
}

