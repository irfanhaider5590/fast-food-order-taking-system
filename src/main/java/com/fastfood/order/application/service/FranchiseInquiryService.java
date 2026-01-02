package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.FranchiseInquiryRequest;
import com.fastfood.order.application.dto.FranchiseInquiryResponse;
import com.fastfood.order.domain.entity.FranchiseInquiry;
import com.fastfood.order.infrastructure.repository.FranchiseInquiryRepository;
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
public class FranchiseInquiryService {

    private final FranchiseInquiryRepository inquiryRepository;

    public FranchiseInquiryResponse createInquiry(FranchiseInquiryRequest request) {
        log.info("Creating franchise inquiry from: {}", request.getEmail());

        FranchiseInquiry inquiry = FranchiseInquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .province(request.getProvince())
                .country(request.getCountry() != null ? request.getCountry() : "Pakistan")
                .investmentRange(request.getInvestmentRange())
                .message(request.getMessage())
                .status(request.getStatus() != null ? request.getStatus() : FranchiseInquiry.InquiryStatus.NEW)
                .notes(request.getNotes())
                .build();

        inquiry = inquiryRepository.save(inquiry);
        return mapToResponse(inquiry);
    }

    public FranchiseInquiryResponse updateInquiry(Long id, FranchiseInquiryRequest request) {
        log.info("Updating franchise inquiry with ID: {}", id);

        FranchiseInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Franchise inquiry not found with ID: " + id));

        inquiry.setName(request.getName());
        inquiry.setEmail(request.getEmail());
        inquiry.setPhone(request.getPhone());
        inquiry.setCity(request.getCity());
        inquiry.setProvince(request.getProvince());
        if (request.getCountry() != null) {
            inquiry.setCountry(request.getCountry());
        }
        inquiry.setInvestmentRange(request.getInvestmentRange());
        inquiry.setMessage(request.getMessage());
        if (request.getStatus() != null) {
            inquiry.setStatus(request.getStatus());
        }
        inquiry.setNotes(request.getNotes());

        inquiry = inquiryRepository.save(inquiry);
        return mapToResponse(inquiry);
    }

    @Transactional(readOnly = true)
    public FranchiseInquiryResponse getInquiryById(Long id) {
        FranchiseInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Franchise inquiry not found with ID: " + id));
        return mapToResponse(inquiry);
    }

    @Transactional(readOnly = true)
    public List<FranchiseInquiryResponse> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FranchiseInquiryResponse> getInquiriesByStatus(FranchiseInquiry.InquiryStatus status) {
        return inquiryRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteInquiry(Long id) {
        log.info("Deleting franchise inquiry with ID: {}", id);
        if (!inquiryRepository.existsById(id)) {
            throw new RuntimeException("Franchise inquiry not found with ID: " + id);
        }
        inquiryRepository.deleteById(id);
    }

    private FranchiseInquiryResponse mapToResponse(FranchiseInquiry inquiry) {
        return FranchiseInquiryResponse.builder()
                .id(inquiry.getId())
                .name(inquiry.getName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .city(inquiry.getCity())
                .province(inquiry.getProvince())
                .country(inquiry.getCountry())
                .investmentRange(inquiry.getInvestmentRange())
                .message(inquiry.getMessage())
                .status(inquiry.getStatus())
                .notes(inquiry.getNotes())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .updatedBy(inquiry.getUpdatedBy())
                .build();
    }
}

