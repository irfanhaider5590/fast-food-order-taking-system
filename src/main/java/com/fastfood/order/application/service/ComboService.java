package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.domain.entity.*;
import com.fastfood.order.infrastructure.repository.*;
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
public class ComboService {

    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final MenuItemRepository menuItemRepository;

    public ComboResponse createCombo(ComboRequest request) {
        log.info("Creating combo: {}", request.getNameEn());
        
        Combo combo = buildCombo(request);
        combo = comboRepository.save(combo);
        saveComboItems(combo, request.getItems());
        
        return getComboById(combo.getId());
    }

    public ComboResponse updateCombo(Long id, ComboRequest request) {
        log.info("Updating combo with ID: {}", id);
        
        Combo combo = findComboById(id);
        updateComboFields(combo, request);
        combo = comboRepository.save(combo);
        updateComboItems(id, combo, request.getItems());
        
        return getComboById(id);
    }

    @Transactional(readOnly = true)
    public ComboResponse getComboById(Long id) {
        Combo combo = findComboById(id);
        List<ComboItem> comboItems = comboItemRepository.findByComboIdOrderByDisplayOrderAsc(id);
        return buildComboResponse(combo, comboItems);
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> getAllCombos() {
        return comboRepository.findAll().stream()
                .map(combo -> getComboById(combo.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> getAvailableCombos() {
        return comboRepository.findByIsAvailableTrueOrderByDisplayOrderAsc().stream()
                .map(combo -> getComboById(combo.getId()))
                .collect(Collectors.toList());
    }

    public void deleteCombo(Long id) {
        log.info("Deleting combo with ID: {}", id);
        validateComboExists(id);
        comboItemRepository.deleteByComboId(id);
        comboRepository.deleteById(id);
    }

    // Private helper methods
    
    private Combo findComboById(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo not found with ID: " + id));
    }

    private void validateComboExists(Long id) {
        if (!comboRepository.existsById(id)) {
            throw new RuntimeException("Combo not found with ID: " + id);
        }
    }

    private Combo buildCombo(ComboRequest request) {
        return Combo.builder()
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .comboPrice(request.getComboPrice())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
    }

    private void updateComboFields(Combo combo, ComboRequest request) {
        combo.setNameEn(request.getNameEn());
        combo.setNameUr(request.getNameUr());
        combo.setDescriptionEn(request.getDescriptionEn());
        combo.setDescriptionUr(request.getDescriptionUr());
        
        if (request.getComboPrice() != null) {
            combo.setComboPrice(request.getComboPrice());
        }
        if (request.getImageUrl() != null) {
            combo.setImageUrl(request.getImageUrl());
        }
        if (request.getIsAvailable() != null) {
            combo.setIsAvailable(request.getIsAvailable());
        }
        if (request.getDisplayOrder() != null) {
            combo.setDisplayOrder(request.getDisplayOrder());
        }
    }

    private void saveComboItems(Combo combo, List<ComboItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return;
        }
        
        List<ComboItem> comboItems = buildComboItems(combo, itemRequests);
        comboItemRepository.saveAll(comboItems);
    }

    private void updateComboItems(Long comboId, Combo combo, List<ComboItemRequest> itemRequests) {
        if (itemRequests == null) {
            return;
        }
        
        comboItemRepository.deleteByComboId(comboId);
        if (!itemRequests.isEmpty()) {
            saveComboItems(combo, itemRequests);
        }
    }

    private List<ComboItem> buildComboItems(Combo combo, List<ComboItemRequest> itemRequests) {
        List<ComboItem> comboItems = new java.util.ArrayList<>();
        int order = 0;
        
        for (ComboItemRequest itemReq : itemRequests) {
            MenuItem menuItem = findMenuItemById(itemReq.getMenuItemId());
            ComboItem comboItem = ComboItem.builder()
                    .combo(combo)
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
                    .displayOrder(itemReq.getDisplayOrder() != null ? itemReq.getDisplayOrder() : order++)
                    .build();
            comboItems.add(comboItem);
        }
        
        return comboItems;
    }

    private MenuItem findMenuItemById(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));
    }

    private ComboResponse buildComboResponse(Combo combo, List<ComboItem> comboItems) {
        return ComboResponse.builder()
                .id(combo.getId())
                .nameEn(combo.getNameEn())
                .nameUr(combo.getNameUr())
                .descriptionEn(combo.getDescriptionEn())
                .descriptionUr(combo.getDescriptionUr())
                .comboPrice(combo.getComboPrice())
                .imageUrl(combo.getImageUrl())
                .isAvailable(combo.getIsAvailable())
                .displayOrder(combo.getDisplayOrder())
                .createdAt(combo.getCreatedAt())
                .updatedAt(combo.getUpdatedAt())
                .items(mapComboItemsToResponse(comboItems))
                .build();
    }

    private List<ComboItemResponse> mapComboItemsToResponse(List<ComboItem> comboItems) {
        return comboItems.stream()
                .map(ci -> ComboItemResponse.builder()
                        .id(ci.getId())
                        .menuItemId(ci.getMenuItem().getId())
                        .menuItemName(ci.getMenuItem().getNameEn())
                        .quantity(ci.getQuantity())
                        .displayOrder(ci.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }
}
