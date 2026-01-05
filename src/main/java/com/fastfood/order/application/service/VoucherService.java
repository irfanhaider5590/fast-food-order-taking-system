package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.VoucherRequest;
import com.fastfood.order.application.dto.VoucherResponse;
import com.fastfood.order.domain.entity.Voucher;
import com.fastfood.order.infrastructure.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherResponse createVoucher(VoucherRequest request) {
        log.info("Creating voucher: {}", request.getCode());

        Voucher voucher = Voucher.builder()
                .code(request.getCode().toUpperCase())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        log.info("Updating voucher with ID: {}", id);

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + id));

        voucher.setCode(request.getCode().toUpperCase());
        voucher.setDescriptionEn(request.getDescriptionEn());
        voucher.setDescriptionUr(request.getDescriptionUr());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidUntil(request.getValidUntil());
        if (request.getIsActive() != null) {
            voucher.setIsActive(request.getIsActive());
        }

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + id));
        return mapToResponse(voucher);
    }

    /**
     * Get all vouchers, optionally filtered by active status
     * @param active Optional filter for active vouchers only
     * @return List of vouchers
     */
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers(Boolean active) {
        if (active != null && active) {
            return getActiveVouchers();
        }
        return getAllVouchers();
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getActiveVouchers() {
        return voucherRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteVoucher(Long id) {
        log.info("Deleting voucher with ID: {}", id);
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Voucher not found with ID: " + id);
        }
        voucherRepository.deleteById(id);
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .descriptionEn(voucher.getDescriptionEn())
                .descriptionUr(voucher.getDescriptionUr())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderAmount(voucher.getMinOrderAmount())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .validFrom(voucher.getValidFrom())
                .validUntil(voucher.getValidUntil())
                .isActive(voucher.getIsActive())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}

