package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.VoucherRequest;
import com.fastfood.order.application.dto.VoucherResponse;
import com.fastfood.order.application.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for voucher operations (Admin only)
 */
@Slf4j
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        log.info("POST /api/vouchers - Creating voucher: {}", request.getCode());
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        log.info("PUT /api/vouchers/{} - Updating voucher", id);
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        log.debug("GET /api/vouchers/{} - Fetching voucher", id);
        VoucherResponse response = voucherService.getVoucherById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VoucherResponse>> getAllVouchers(
            @RequestParam(required = false) Boolean active) {
        log.debug("GET /api/vouchers - active={}", active);
        List<VoucherResponse> vouchers = voucherService.getAllVouchers(active);
        return ResponseEntity.ok(vouchers);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        log.info("DELETE /api/vouchers/{} - Deleting voucher", id);
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}

