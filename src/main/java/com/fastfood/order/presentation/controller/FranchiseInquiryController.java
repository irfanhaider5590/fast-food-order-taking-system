package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.FranchiseInquiryRequest;
import com.fastfood.order.application.dto.FranchiseInquiryResponse;
import com.fastfood.order.application.service.FranchiseInquiryService;
import com.fastfood.order.domain.entity.FranchiseInquiry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/franchise-inquiries")
@RequiredArgsConstructor
public class FranchiseInquiryController {

    private final FranchiseInquiryService inquiryService;

    @PostMapping
    public ResponseEntity<FranchiseInquiryResponse> createInquiry(@Valid @RequestBody FranchiseInquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryService.createInquiry(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FranchiseInquiryResponse> updateInquiry(
            @PathVariable Long id,
            @Valid @RequestBody FranchiseInquiryRequest request) {
        return ResponseEntity.ok(inquiryService.updateInquiry(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FranchiseInquiryResponse> getInquiryById(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getInquiryById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FranchiseInquiryResponse>> getAllInquiries(
            @RequestParam(required = false) String status) {
        if (status != null) {
            try {
                FranchiseInquiry.InquiryStatus inquiryStatus = FranchiseInquiry.InquiryStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(inquiryService.getInquiriesByStatus(inquiryStatus));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.noContent().build();
    }
}

