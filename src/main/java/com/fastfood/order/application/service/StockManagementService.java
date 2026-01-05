package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.domain.entity.*;
import com.fastfood.order.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockManagementService {

    private final StockItemRepository stockItemRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    public List<StockItemResponse> getAllStockItems() {
        return stockItemRepository.findAll().stream()
                .map(this::mapToStockItemResponse)
                .collect(Collectors.toList());
    }

    public List<StockItemResponse> getActiveStockItems() {
        return stockItemRepository.findByIsActiveTrueOrderByNameEnAsc().stream()
                .map(this::mapToStockItemResponse)
                .collect(Collectors.toList());
    }

    public StockItemResponse getStockItemById(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + id));
        return mapToStockItemResponse(stockItem);
    }

    @Transactional
    public StockItemResponse createStockItem(StockItemRequest request) {
        StockItem stockItem = StockItem.builder()
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .unit(request.getUnit() != null ? request.getUnit() : "piece")
                .currentQuantity(request.getCurrentQuantity())
                .minThreshold(request.getMinThreshold())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Created stock item: {}", saved.getNameEn());
        return mapToStockItemResponse(saved);
    }

    @Transactional
    public StockItemResponse updateStockItem(Long id, StockItemRequest request) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + id));

        stockItem.setNameEn(request.getNameEn());
        stockItem.setNameUr(request.getNameUr());
        stockItem.setDescriptionEn(request.getDescriptionEn());
        stockItem.setDescriptionUr(request.getDescriptionUr());
        if (request.getUnit() != null) {
            stockItem.setUnit(request.getUnit());
        }
        stockItem.setCurrentQuantity(request.getCurrentQuantity());
        stockItem.setMinThreshold(request.getMinThreshold());
        if (request.getIsActive() != null) {
            stockItem.setIsActive(request.getIsActive());
        }

        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Updated stock item: {}", saved.getNameEn());
        return mapToStockItemResponse(saved);
    }

    @Transactional
    public void deleteStockItem(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + id));
        stockItemRepository.delete(stockItem);
        log.info("Deleted stock item: {}", stockItem.getNameEn());
    }

    public List<MenuItemIngredientResponse> getMenuItemIngredients(Long menuItemId) {
        return menuItemIngredientRepository.findByMenuItemId(menuItemId).stream()
                .map(this::mapToIngredientResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveMenuItemIngredients(Long menuItemId, List<MenuItemIngredientRequest> ingredients) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

        // Delete existing ingredients
        menuItemIngredientRepository.deleteByMenuItemId(menuItemId);

        // Check for duplicates in the input list
        Map<Long, MenuItemIngredientRequest> uniqueIngredients = new HashMap<>();
        for (MenuItemIngredientRequest req : ingredients) {
            if (req.getStockItemId() == null || req.getStockItemId() <= 0) {
                continue; // Skip invalid entries
            }
            
            if (uniqueIngredients.containsKey(req.getStockItemId())) {
                throw new RuntimeException("Duplicate stock item found in ingredients list. Stock item ID: " + req.getStockItemId());
            }
            uniqueIngredients.put(req.getStockItemId(), req);
        }

        // Save new ingredients
        for (MenuItemIngredientRequest req : uniqueIngredients.values()) {
            StockItem stockItem = stockItemRepository.findById(req.getStockItemId())
                    .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + req.getStockItemId()));

            MenuItemIngredient ingredient = MenuItemIngredient.builder()
                    .menuItem(menuItem)
                    .stockItem(stockItem)
                    .quantityRequired(req.getQuantityRequired())
                    .build();

            menuItemIngredientRepository.save(ingredient);
        }

        log.info("Saved {} ingredients for menu item ID: {}", uniqueIngredients.size(), menuItemId);
    }

    @Transactional
    public void deductStockForOrder(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getMenuItem() != null) {
                deductStockForMenuItem(orderItem.getMenuItem(), orderItem.getQuantity(), order.getId());
            }
        }

        log.info("Stock deducted for order: {}", order.getOrderNumber());
    }

    @Transactional
    private void deductStockForMenuItem(MenuItem menuItem, Integer quantity, Long orderId) {
        List<MenuItemIngredient> ingredients = menuItemIngredientRepository.findByMenuItemId(menuItem.getId());
        
        for (MenuItemIngredient ingredient : ingredients) {
            StockItem stockItem = ingredient.getStockItem();
            BigDecimal quantityToDeduct = ingredient.getQuantityRequired()
                    .multiply(BigDecimal.valueOf(quantity));

            BigDecimal previousQuantity = stockItem.getCurrentQuantity();
            BigDecimal newQuantity = previousQuantity.subtract(quantityToDeduct);

            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Stock quantity would go negative for {}: {} -> {}. Setting to 0.",
                        stockItem.getNameEn(), previousQuantity, newQuantity);
                newQuantity = BigDecimal.ZERO;
            }

            stockItem.setCurrentQuantity(newQuantity);
            stockItemRepository.save(stockItem);

            // Create transaction record
            StockTransaction transaction = StockTransaction.builder()
                    .stockItem(stockItem)
                    .transactionType(StockTransaction.TransactionType.SALE)
                    .quantity(quantityToDeduct)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .referenceType("ORDER")
                    .referenceId(orderId)
                    .notes("Deducted for menu item: " + menuItem.getNameEn())
                    .build();

            stockTransactionRepository.save(transaction);
        }
    }

    @Transactional
    public void adjustStock(Long stockItemId, BigDecimal quantity, String notes) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new RuntimeException("Stock item not found"));

        BigDecimal previousQuantity = stockItem.getCurrentQuantity();
        BigDecimal newQuantity = previousQuantity.add(quantity);

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Stock adjustment would result in negative quantity");
        }

        stockItem.setCurrentQuantity(newQuantity);
        stockItemRepository.save(stockItem);

        StockTransaction transaction = StockTransaction.builder()
                .stockItem(stockItem)
                .transactionType(StockTransaction.TransactionType.ADJUSTMENT)
                .quantity(quantity)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .referenceType("MANUAL")
                .notes(notes)
                .build();

        stockTransactionRepository.save(transaction);
        log.info("Adjusted stock for {}: {} -> {}", stockItem.getNameEn(), previousQuantity, newQuantity);
    }

    private StockItemResponse mapToStockItemResponse(StockItem stockItem) {
        return StockItemResponse.builder()
                .id(stockItem.getId())
                .nameEn(stockItem.getNameEn())
                .nameUr(stockItem.getNameUr())
                .descriptionEn(stockItem.getDescriptionEn())
                .descriptionUr(stockItem.getDescriptionUr())
                .unit(stockItem.getUnit())
                .currentQuantity(stockItem.getCurrentQuantity())
                .minThreshold(stockItem.getMinThreshold())
                .isActive(stockItem.getIsActive())
                .isLowStock(stockItem.isLowStock())
                .createdAt(stockItem.getCreatedAt())
                .updatedAt(stockItem.getUpdatedAt())
                .build();
    }

    private MenuItemIngredientResponse mapToIngredientResponse(MenuItemIngredient ingredient) {
        StockItem stockItem = ingredient.getStockItem();
        return MenuItemIngredientResponse.builder()
                .id(ingredient.getId())
                .stockItemId(stockItem.getId())
                .stockItemNameEn(stockItem.getNameEn())
                .stockItemNameUr(stockItem.getNameUr())
                .stockItemUnit(stockItem.getUnit())
                .quantityRequired(ingredient.getQuantityRequired())
                .build();
    }
}

