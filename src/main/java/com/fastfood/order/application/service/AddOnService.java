package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.AddOnRequest;
import com.fastfood.order.application.dto.AddOnResponse;
import com.fastfood.order.domain.entity.AddOn;
import com.fastfood.order.infrastructure.repository.AddOnRepository;
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
public class AddOnService {

    private final AddOnRepository addOnRepository;

    public AddOnResponse createAddOn(AddOnRequest request) {
        log.info("Creating add-on: {}", request.getNameEn());

        AddOn addOn = AddOn.builder()
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .price(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO)
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        addOn = addOnRepository.save(addOn);
        return mapToResponse(addOn);
    }

    public AddOnResponse updateAddOn(Long id, AddOnRequest request) {
        log.info("Updating add-on with ID: {}", id);

        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Add-on not found with ID: " + id));

        addOn.setNameEn(request.getNameEn());
        addOn.setNameUr(request.getNameUr());
        addOn.setDescriptionEn(request.getDescriptionEn());
        addOn.setDescriptionUr(request.getDescriptionUr());
        if (request.getPrice() != null) {
            addOn.setPrice(request.getPrice());
        }
        if (request.getIsAvailable() != null) {
            addOn.setIsAvailable(request.getIsAvailable());
        }
        if (request.getDisplayOrder() != null) {
            addOn.setDisplayOrder(request.getDisplayOrder());
        }

        addOn = addOnRepository.save(addOn);
        return mapToResponse(addOn);
    }

    @Transactional(readOnly = true)
    public AddOnResponse getAddOnById(Long id) {
        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Add-on not found with ID: " + id));
        return mapToResponse(addOn);
    }

    @Transactional(readOnly = true)
    public List<AddOnResponse> getAllAddOns() {
        return addOnRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AddOnResponse> getAvailableAddOns() {
        return addOnRepository.findByIsAvailableTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteAddOn(Long id) {
        log.info("Deleting add-on with ID: {}", id);
        if (!addOnRepository.existsById(id)) {
            throw new RuntimeException("Add-on not found with ID: " + id);
        }
        addOnRepository.deleteById(id);
    }

    private AddOnResponse mapToResponse(AddOn addOn) {
        return AddOnResponse.builder()
                .id(addOn.getId())
                .nameEn(addOn.getNameEn())
                .nameUr(addOn.getNameUr())
                .descriptionEn(addOn.getDescriptionEn())
                .descriptionUr(addOn.getDescriptionUr())
                .price(addOn.getPrice())
                .isAvailable(addOn.getIsAvailable())
                .displayOrder(addOn.getDisplayOrder())
                .createdAt(addOn.getCreatedAt())
                .updatedAt(addOn.getUpdatedAt())
                .build();
    }
}

