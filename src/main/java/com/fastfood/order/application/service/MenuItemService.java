package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.domain.entity.*;
import com.fastfood.order.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemSizeRepository sizeRepository;
    private final MenuItemAddOnRepository menuItemAddOnRepository;
    private final AddOnRepository addOnRepository;

    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info("Creating menu item: {}", request.getNameEn());
        
        MenuCategory category = findCategoryById(request.getCategoryId());
        MenuItem menuItem = buildMenuItem(request, category);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        
        saveMenuItemSizes(savedMenuItem, request.getSizes());
        saveMenuItemAddOns(savedMenuItem, request.getAddOnIds());
        
        return getMenuItemById(savedMenuItem.getId());
    }

    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        log.info("Updating menu item with ID: {}", id);
        log.info("Received request - sizes: {}, addOnIds: {}", 
                request.getSizes() != null ? request.getSizes().size() : "null",
                request.getAddOnIds() != null ? request.getAddOnIds().size() : "null");
        
        MenuItem menuItem = findMenuItemById(id);
        updateMenuItemFields(menuItem, request);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        
        updateMenuItemSizes(id, savedMenuItem, request.getSizes());
        updateMenuItemAddOns(id, savedMenuItem, request.getAddOnIds());
        
        return getMenuItemById(id);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = findMenuItemById(id);
        List<MenuItemSize> sizes = sizeRepository.findByMenuItemIdOrderByDisplayOrderAsc(id);
        List<AddOn> availableAddOns = getAvailableAddOnsForMenuItem(id);
        
        return buildMenuItemResponse(menuItem, sizes, availableAddOns);
    }

    @Transactional(readOnly = true)
    /**
     * Get all menu items, optionally filtered by category
     * @param categoryId Optional category ID to filter by
     * @return List of menu items
     */
    public List<MenuItemResponse> getAllMenuItems(Long categoryId) {
        if (categoryId != null) {
            return getMenuItemsByCategory(categoryId);
        }
        return getAllMenuItems();
    }

    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(item -> {
                    List<MenuItemSize> sizes = sizeRepository.findByMenuItemIdOrderByDisplayOrderAsc(item.getId());
                    List<AddOn> availableAddOns = getAvailableAddOnsForMenuItem(item.getId());
                    return buildMenuItemResponse(item, sizes, availableAddOns);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryIdOrderByDisplayOrder(categoryId).stream()
                .map(item -> getMenuItemById(item.getId()))
                .collect(Collectors.toList());
    }

    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item with ID: {}", id);
        validateMenuItemExists(id);
        sizeRepository.deleteByMenuItemId(id);
        menuItemAddOnRepository.deleteByMenuItemId(id);
        menuItemRepository.deleteById(id);
    }

    // Private helper methods
    
    private MenuCategory findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Menu category not found with ID: " + categoryId));
    }

    private MenuItem findMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
    }

    private void validateMenuItemExists(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Menu item not found with ID: " + id);
        }
    }

    private MenuItem buildMenuItem(MenuItemRequest request, MenuCategory category) {
        return MenuItem.builder()
                .category(category)
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .basePrice(request.getBasePrice())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isCombo(request.getIsCombo() != null ? request.getIsCombo() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
    }

    private void updateMenuItemFields(MenuItem menuItem, MenuItemRequest request) {
        if (request.getCategoryId() != null && !request.getCategoryId().equals(menuItem.getCategory().getId())) {
            MenuCategory category = findCategoryById(request.getCategoryId());
            menuItem.setCategory(category);
        }
        
        menuItem.setNameEn(request.getNameEn());
        menuItem.setNameUr(request.getNameUr());
        menuItem.setDescriptionEn(request.getDescriptionEn());
        menuItem.setDescriptionUr(request.getDescriptionUr());
        
        if (request.getBasePrice() != null) {
            menuItem.setBasePrice(request.getBasePrice());
        }
        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsCombo() != null) {
            menuItem.setIsCombo(request.getIsCombo());
        }
        if (request.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(request.getDisplayOrder());
        }
    }

    private void saveMenuItemSizes(MenuItem menuItem, List<MenuItemSizeRequest> sizeRequests) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            log.info("No sizes provided for menu item {}", menuItem.getId());
            return;
        }
        
        log.info("Processing {} sizes for menu item {}", sizeRequests.size(), menuItem.getId());
        List<MenuItemSize> validSizes = filterAndBuildSizes(menuItem, sizeRequests);
        
        if (!validSizes.isEmpty()) {
            log.info("Saving {} valid sizes", validSizes.size());
            sizeRepository.saveAll(validSizes);
            log.info("Successfully saved {} sizes", validSizes.size());
        } else {
            log.warn("No valid sizes to save after filtering");
        }
    }

    private void updateMenuItemSizes(Long menuItemId, MenuItem menuItem, List<MenuItemSizeRequest> sizeRequests) {
        log.info("updateMenuItemSizes called for menu item ID: {}, sizes: {}", 
                menuItemId, sizeRequests != null ? sizeRequests.size() : "null");
        
        if (sizeRequests == null) {
            log.warn("No sizes provided in request (null) for menu item ID: {}", menuItemId);
            return;
        }
        
        log.info("Processing sizes update for menu item ID: {}. Received {} sizes", menuItemId, sizeRequests.size());
        
        if (sizeRequests.isEmpty()) {
            log.info("Sizes list is empty for menu item ID: {}. Deleting existing sizes.", menuItemId);
            sizeRepository.deleteByMenuItemId(menuItemId);
            return;
        }
        
        // Log each size request for debugging
        for (int i = 0; i < sizeRequests.size(); i++) {
            MenuItemSizeRequest sizeReq = sizeRequests.get(i);
            log.info("Size[{}]: code='{}', name='{}', modifier={}", 
                    i, sizeReq.getSizeCode(), sizeReq.getSizeNameEn(), sizeReq.getPriceModifier());
        }
        
        sizeRepository.deleteByMenuItemId(menuItemId);
        log.info("Deleted existing sizes for menu item ID: {}", menuItemId);
        
        List<MenuItemSize> validSizes = filterAndBuildSizes(menuItem, sizeRequests);
        log.info("After filtering: {} valid sizes out of {} total", validSizes.size(), sizeRequests.size());
        
        if (!validSizes.isEmpty()) {
            log.info("Saving {} valid sizes for menu item ID: {}", validSizes.size(), menuItemId);
            sizeRepository.saveAll(validSizes);
            log.info("Successfully saved {} sizes for menu item ID: {}", validSizes.size(), menuItemId);
        } else {
            log.warn("No valid sizes to save after filtering for menu item ID: {}", menuItemId);
        }
    }

    private List<MenuItemSize> filterAndBuildSizes(MenuItem menuItem, List<MenuItemSizeRequest> sizeRequests) {
        return sizeRequests.stream()
                .filter(this::isValidSizeRequest)
                .map(sizeReq -> buildMenuItemSize(menuItem, sizeReq))
                .collect(Collectors.toList());
    }

    private boolean isValidSizeRequest(MenuItemSizeRequest sizeReq) {
        boolean valid = sizeReq.getSizeCode() != null && !sizeReq.getSizeCode().trim().isEmpty()
                && sizeReq.getSizeNameEn() != null && !sizeReq.getSizeNameEn().trim().isEmpty();
        if (!valid) {
            log.warn("Skipping invalid size: code='{}', name='{}'", 
                    sizeReq.getSizeCode(), sizeReq.getSizeNameEn());
        }
        return valid;
    }

    private MenuItemSize buildMenuItemSize(MenuItem menuItem, MenuItemSizeRequest sizeReq) {
        log.debug("Creating size: code='{}', name='{}', modifier={}", 
                sizeReq.getSizeCode(), sizeReq.getSizeNameEn(), sizeReq.getPriceModifier());
        return MenuItemSize.builder()
                .menuItem(menuItem)
                .sizeCode(sizeReq.getSizeCode().trim().toUpperCase())
                .sizeNameEn(sizeReq.getSizeNameEn().trim())
                .sizeNameUr(sizeReq.getSizeNameUr() != null ? sizeReq.getSizeNameUr().trim() : null)
                .priceModifier(sizeReq.getPriceModifier() != null ? sizeReq.getPriceModifier() : BigDecimal.ZERO)
                .isAvailable(sizeReq.getIsAvailable() != null ? sizeReq.getIsAvailable() : true)
                .displayOrder(sizeReq.getDisplayOrder() != null ? sizeReq.getDisplayOrder() : 0)
                .build();
    }

    private void saveMenuItemAddOns(MenuItem menuItem, List<Long> addOnIds) {
        if (addOnIds == null || addOnIds.isEmpty()) {
            return;
        }
        
        List<MenuItemAddOn> menuItemAddOns = addOnIds.stream()
                .map(addOnId -> {
                    AddOn addOn = addOnRepository.findById(addOnId)
                            .orElseThrow(() -> new RuntimeException("Add-on not found with ID: " + addOnId));
                    return MenuItemAddOn.builder()
                            .menuItem(menuItem)
                            .addOn(addOn)
                            .isDefault(false)
                            .build();
                })
                .collect(Collectors.toList());
        menuItemAddOnRepository.saveAll(menuItemAddOns);
    }

    private void updateMenuItemAddOns(Long menuItemId, MenuItem menuItem, List<Long> addOnIds) {
        if (addOnIds == null) {
            return;
        }
        
        menuItemAddOnRepository.deleteByMenuItemId(menuItemId);
        if (!addOnIds.isEmpty()) {
            saveMenuItemAddOns(menuItem, addOnIds);
        }
    }

    private List<AddOn> getAvailableAddOnsForMenuItem(Long menuItemId) {
        List<MenuItemAddOn> menuItemAddOns = menuItemAddOnRepository.findByMenuItemId(menuItemId);
        return menuItemAddOns.stream()
                .map(MenuItemAddOn::getAddOn)
                .collect(Collectors.toList());
    }

    private MenuItemResponse buildMenuItemResponse(MenuItem menuItem, List<MenuItemSize> sizes, List<AddOn> availableAddOns) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .categoryId(menuItem.getCategory().getId())
                .categoryName(menuItem.getCategory().getNameEn())
                .nameEn(menuItem.getNameEn())
                .nameUr(menuItem.getNameUr())
                .descriptionEn(menuItem.getDescriptionEn())
                .descriptionUr(menuItem.getDescriptionUr())
                .basePrice(menuItem.getBasePrice())
                .imageUrl(menuItem.getImageUrl())
                .isAvailable(menuItem.getIsAvailable())
                .isCombo(menuItem.getIsCombo())
                .displayOrder(menuItem.getDisplayOrder())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .sizes(mapSizesToResponse(sizes))
                .availableAddOns(mapAddOnsToResponse(availableAddOns))
                .build();
    }

    private List<MenuItemSizeResponse> mapSizesToResponse(List<MenuItemSize> sizes) {
        return sizes.stream()
                .map(s -> MenuItemSizeResponse.builder()
                        .id(s.getId())
                        .sizeCode(s.getSizeCode())
                        .sizeNameEn(s.getSizeNameEn())
                        .sizeNameUr(s.getSizeNameUr())
                        .priceModifier(s.getPriceModifier())
                        .isAvailable(s.getIsAvailable())
                        .displayOrder(s.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AddOnResponse> mapAddOnsToResponse(List<AddOn> addOns) {
        return addOns.stream()
                .map(a -> AddOnResponse.builder()
                        .id(a.getId())
                        .nameEn(a.getNameEn())
                        .nameUr(a.getNameUr())
                        .descriptionEn(a.getDescriptionEn())
                        .descriptionUr(a.getDescriptionUr())
                        .price(a.getPrice())
                        .isAvailable(a.getIsAvailable())
                        .displayOrder(a.getDisplayOrder())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
