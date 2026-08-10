package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.*;
import com.fastfood.order.domain.entity.*;
import com.fastfood.order.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockManagementService {

    private final StockItemRepository stockItemRepository;
    private final StockItemConsumptionRepository stockItemConsumptionRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemSizeRepository menuItemSizeRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ComboItemRepository comboItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopContextService shopContextService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<StockItemResponse> getAllStockItems() {
        Long shopId = shopContextService.requireCurrentShopId();
        return stockItemRepository.findByShopIdOrderByNameEnAsc(shopId).stream()
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
                .shop(shopContextService.requireCurrentShop())
                .nameEn(request.getNameEn())
                .nameUr(request.getNameUr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionUr(request.getDescriptionUr())
                .unit(request.getUnit() != null ? request.getUnit() : "piece")
                .currentQuantity(request.getCurrentQuantity() != null ? request.getCurrentQuantity() : BigDecimal.ZERO)
                .minThreshold(request.getMinThreshold())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .barcode(normalizeBarcode(request.getBarcode()))
                .scanPackQty(request.getScanPackQty() != null && request.getScanPackQty().compareTo(BigDecimal.ZERO) > 0
                        ? request.getScanPackQty()
                        : BigDecimal.ONE)
                .build();

        ensureBarcodeUnique(savedShopId(stockItem), stockItem.getBarcode(), null);

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
        // Catalog updates keep existing quantity unless explicitly provided
        if (request.getCurrentQuantity() != null) {
            stockItem.setCurrentQuantity(request.getCurrentQuantity());
        }
        stockItem.setMinThreshold(request.getMinThreshold());
        if (request.getIsActive() != null) {
            stockItem.setIsActive(request.getIsActive());
        }
        String barcode = normalizeBarcode(request.getBarcode());
        ensureBarcodeUnique(savedShopId(stockItem), barcode, stockItem.getId());
        stockItem.setBarcode(barcode);
        if (request.getScanPackQty() != null && request.getScanPackQty().compareTo(BigDecimal.ZERO) > 0) {
            stockItem.setScanPackQty(request.getScanPackQty());
        } else if (stockItem.getScanPackQty() == null) {
            stockItem.setScanPackQty(BigDecimal.ONE);
        }

        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Updated stock item: {}", saved.getNameEn());
        return mapToStockItemResponse(saved);
    }

    /**
     * Set absolute on-hand quantity (staff quantity screen).
     */
    @Transactional
    public StockItemResponse setStockQuantity(Long stockItemId, BigDecimal quantity, String notes) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Quantity must be zero or greater");
        }
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new RuntimeException("Stock item not found"));

        BigDecimal previousQuantity = stockItem.getCurrentQuantity();
        BigDecimal delta = quantity.subtract(previousQuantity);
        stockItem.setCurrentQuantity(quantity);
        stockItemRepository.save(stockItem);

        stockTransactionRepository.save(StockTransaction.builder()
                .stockItem(stockItem)
                .transactionType(StockTransaction.TransactionType.ADJUSTMENT)
                .quantity(delta)
                .previousQuantity(previousQuantity)
                .newQuantity(quantity)
                .referenceType("MANUAL")
                .notes(notes != null ? notes : "Quantity set")
                .build());

        log.info("Set stock quantity for {}: {} -> {}", stockItem.getNameEn(), previousQuantity, quantity);
        return mapToStockItemResponse(stockItem);
    }

    @Transactional
    public void deleteStockItem(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + id));
        stockItemRepository.delete(stockItem);
        log.info("Deleted stock item: {}", stockItem.getNameEn());
    }

    @Transactional(readOnly = true)
    public StockConsumptionConfigResponse getStockConsumptions(Long stockItemId) {
        StockItem stockItem = requireShopStockItem(stockItemId);
        List<StockItemConsumption> rows = stockItemConsumptionRepository.findDetailedByStockItemId(stockItemId);

        Map<String, String> sizeNames = new HashMap<>();
        for (StockItemConsumption row : rows) {
            if (row.getSizeCode() != null && !row.getSizeCode().isBlank()) {
                menuItemSizeRepository.findByMenuItemIdAndSizeCode(row.getMenuItem().getId(), row.getSizeCode())
                        .ifPresent(s -> sizeNames.put(row.getMenuItem().getId() + "|" + row.getSizeCode(), s.getSizeNameEn()));
            }
        }

        List<StockConsumptionRowResponse> mapped = rows.stream()
                .map(row -> {
                    MenuItem mi = row.getMenuItem();
                    String sizeKey = mi.getId() + "|" + row.getSizeCode();
                    return StockConsumptionRowResponse.builder()
                            .id(row.getId())
                            .menuItemId(mi.getId())
                            .menuItemNameEn(mi.getNameEn())
                            .categoryId(mi.getCategory() != null ? mi.getCategory().getId() : null)
                            .categoryNameEn(mi.getCategory() != null ? mi.getCategory().getNameEn() : null)
                            .sizeCode(row.getSizeCode())
                            .sizeNameEn(row.getSizeCode() != null ? sizeNames.get(sizeKey) : null)
                            .servingsPerUnit(row.getServingsPerUnit())
                            .quantityPerServing(row.getQuantityPerServing())
                            .build();
                })
                .collect(Collectors.toList());

        return StockConsumptionConfigResponse.builder()
                .stockItemId(stockItem.getId())
                .stockItemNameEn(stockItem.getNameEn())
                .stockItemNameUr(stockItem.getNameUr())
                .unit(stockItem.getUnit())
                .currentQuantity(stockItem.getCurrentQuantity())
                .minThreshold(stockItem.getMinThreshold())
                .rows(mapped)
                .build();
    }

    @Transactional
    public StockConsumptionConfigResponse saveStockConsumptions(Long stockItemId, List<StockConsumptionRowRequest> rows) {
        StockItem stockItem = requireShopStockItem(stockItemId);

        // Bulk delete + clear session so managed entities cannot be re-inserted on flush
        stockItemConsumptionRepository.deleteByStockItemId(stockItemId);
        entityManager.flush();
        entityManager.clear();
        // Re-attach stock item after clear
        stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + stockItemId));

        if (rows == null) {
            rows = List.of();
        }

        Set<String> seen = new HashSet<>();
        List<StockItemConsumption> toSave = new ArrayList<>();
        for (StockConsumptionRowRequest req : rows) {
            if (req.getMenuItemId() == null || req.getServingsPerUnit() == null
                    || req.getServingsPerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + req.getMenuItemId()));

            String sizeCode = normalizeSizeCode(req.getSizeCode());
            String key = menuItem.getId() + "|" + (sizeCode == null ? "" : sizeCode);
            if (!seen.add(key)) {
                throw new RuntimeException("Duplicate consumption row for menu item " + menuItem.getNameEn()
                        + (sizeCode != null ? " / " + sizeCode : ""));
            }

            if (sizeCode != null) {
                menuItemSizeRepository.findByMenuItemIdAndSizeCode(menuItem.getId(), sizeCode)
                        .orElseThrow(() -> new RuntimeException(
                                "Size " + sizeCode + " not found for menu item " + menuItem.getNameEn()));
            }

            BigDecimal servings = req.getServingsPerUnit().setScale(4, RoundingMode.HALF_UP);
            BigDecimal qtyPerServing = BigDecimal.ONE.divide(servings, 6, RoundingMode.HALF_UP);

            toSave.add(StockItemConsumption.builder()
                    .stockItem(stockItem)
                    .menuItem(menuItem)
                    .sizeCode(sizeCode)
                    .servingsPerUnit(servings)
                    .quantityPerServing(qtyPerServing)
                    .build());
        }

        stockItemConsumptionRepository.saveAll(toSave);
        stockItemConsumptionRepository.flush();
        log.info("Saved {} consumption rows for stock item {}", toSave.size(), stockItem.getNameEn());
        return getStockConsumptions(stockItemId);
    }

    @Transactional(readOnly = true)
    public ConsumptionCatalogResponse getConsumptionCatalog() {
        Long shopId = shopContextService.requireCurrentShopId();
        List<MenuCategory> categories = menuCategoryRepository.findByShopIdAndIsActiveTrueOrderByDisplayOrderAsc(shopId);
        List<MenuItem> items = menuItemRepository.findByShopIdOrderByDisplayOrderAsc(shopId).stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable()) && !Boolean.TRUE.equals(mi.getIsCombo()))
                .toList();

        Map<Long, List<MenuItem>> byCategory = items.stream()
                .filter(mi -> mi.getCategory() != null)
                .collect(Collectors.groupingBy(mi -> mi.getCategory().getId()));

        List<ConsumptionCatalogResponse.ConsumptionCatalogCategory> catalogCategories = new ArrayList<>();
        for (MenuCategory category : categories) {
            List<MenuItem> categoryItems = byCategory.getOrDefault(category.getId(), List.of());
            if (categoryItems.isEmpty()) {
                continue;
            }

            List<ConsumptionCatalogResponse.ConsumptionCatalogItem> catalogItems = new ArrayList<>();
            for (MenuItem mi : categoryItems) {
                List<MenuItemSize> sizes = menuItemSizeRepository
                        .findByMenuItemIdAndIsAvailableTrueOrderByDisplayOrderAsc(mi.getId());
                catalogItems.add(ConsumptionCatalogResponse.ConsumptionCatalogItem.builder()
                        .id(mi.getId())
                        .nameEn(mi.getNameEn())
                        .categoryId(category.getId())
                        .sizes(sizes.stream()
                                .map(s -> ConsumptionCatalogResponse.ConsumptionCatalogSize.builder()
                                        .sizeCode(s.getSizeCode())
                                        .sizeNameEn(s.getSizeNameEn())
                                        .build())
                                .collect(Collectors.toList()))
                        .build());
            }

            catalogCategories.add(ConsumptionCatalogResponse.ConsumptionCatalogCategory.builder()
                    .id(category.getId())
                    .nameEn(category.getNameEn())
                    .items(catalogItems)
                    .build());
        }

        return ConsumptionCatalogResponse.builder().categories(catalogCategories).build();
    }

    /**
     * Prefetch shop consumption rates once, aggregate deductions in memory, then apply.
     * Size match: merge exact (menuItemId, sizeCode) with any-size (menuItemId, null);
     * if the same stock item has both rows, exact wins.
     */
    @Transactional
    public void deductStockForOrder(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        Long shopId = order.getShop() != null
                ? order.getShop().getId()
                : shopContextService.requireCurrentShopId();

        List<StockItemConsumption> rates = stockItemConsumptionRepository.findActiveByShopId(shopId);
        // key: menuItemId|SIZE or menuItemId| → list of rates
        Map<String, List<StockItemConsumption>> rateIndex = new HashMap<>();
        for (StockItemConsumption rate : rates) {
            String key = rateKey(rate.getMenuItem().getId(), rate.getSizeCode());
            rateIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(rate);
        }

        // stockItemId -> total quantity to deduct
        Map<Long, BigDecimal> deductTotals = new HashMap<>();
        // stockItemId -> notes fragments
        Map<Long, StringBuilder> notes = new HashMap<>();
        Map<Long, StockItem> stockById = new HashMap<>();

        for (OrderItem orderItem : orderItems) {
            int lineQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : 1;

            if (orderItem.getCombo() != null) {
                List<ComboItem> comboItems = comboItemRepository.findByComboIdOrderByDisplayOrderAsc(orderItem.getCombo().getId());
                if (comboItems == null || comboItems.isEmpty()) {
                    log.debug("Combo {} has no item breakdown; skipping stock deduct", orderItem.getCombo().getId());
                    continue;
                }
                for (ComboItem ci : comboItems) {
                    if (ci.getMenuItem() == null) {
                        continue;
                    }
                    int componentQty = lineQty * (ci.getQuantity() != null ? ci.getQuantity() : 1);
                    accumulateDeduction(ci.getMenuItem().getId(), null, componentQty, rateIndex,
                            deductTotals, notes, stockById, ci.getMenuItem().getNameEn());
                }
            } else if (orderItem.getMenuItem() != null) {
                accumulateDeduction(
                        orderItem.getMenuItem().getId(),
                        normalizeSizeCode(orderItem.getSizeCode()),
                        lineQty,
                        rateIndex,
                        deductTotals,
                        notes,
                        stockById,
                        orderItem.getMenuItem().getNameEn());
            }
        }

        if (deductTotals.isEmpty()) {
            log.info("No consumption rates matched for order: {}", order.getOrderNumber());
            return;
        }

        List<StockTransaction> transactions = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : deductTotals.entrySet()) {
            Long stockItemId = entry.getKey();
            BigDecimal quantityToDeduct = entry.getValue();
            StockItem stockItem = stockById.get(stockItemId);
            if (stockItem == null) {
                continue;
            }

            BigDecimal previousQuantity = stockItem.getCurrentQuantity();
            BigDecimal newQuantity = previousQuantity.subtract(quantityToDeduct);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Stock quantity would go negative for {}: {} -> {}. Setting to 0.",
                        stockItem.getNameEn(), previousQuantity, newQuantity);
                newQuantity = BigDecimal.ZERO;
            }

            stockItem.setCurrentQuantity(newQuantity);
            stockItemRepository.save(stockItem);

            String note = notes.containsKey(stockItemId)
                    ? "Sale deduct: " + notes.get(stockItemId)
                    : "Deducted for order";
            transactions.add(StockTransaction.builder()
                    .stockItem(stockItem)
                    .transactionType(StockTransaction.TransactionType.SALE)
                    .quantity(quantityToDeduct)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .referenceType("ORDER")
                    .referenceId(order.getId())
                    .notes(note.length() > 500 ? note.substring(0, 500) : note)
                    .build());
        }

        stockTransactionRepository.saveAll(transactions);
        log.info("Stock deducted for order {}: {} stock items updated", order.getOrderNumber(), transactions.size());
    }

    private void accumulateDeduction(
            Long menuItemId,
            String sizeCode,
            int quantity,
            Map<String, List<StockItemConsumption>> rateIndex,
            Map<Long, BigDecimal> deductTotals,
            Map<Long, StringBuilder> notes,
            Map<Long, StockItem> stockById,
            String menuItemName) {

        // Exact size rates + any-size (null) rates. If the same stock item has both,
        // prefer the exact size row (do not double-deduct).
        List<StockItemConsumption> exact = sizeCode != null
                ? rateIndex.get(rateKey(menuItemId, sizeCode))
                : null;
        List<StockItemConsumption> anySize = rateIndex.get(rateKey(menuItemId, null));

        Map<Long, StockItemConsumption> byStockId = new LinkedHashMap<>();
        if (anySize != null) {
            for (StockItemConsumption rate : anySize) {
                byStockId.put(rate.getStockItem().getId(), rate);
            }
        }
        if (exact != null) {
            for (StockItemConsumption rate : exact) {
                byStockId.put(rate.getStockItem().getId(), rate);
            }
        }

        if (byStockId.isEmpty()) {
            return;
        }

        for (StockItemConsumption rate : byStockId.values()) {
            StockItem stockItem = rate.getStockItem();
            Long stockId = stockItem.getId();
            stockById.putIfAbsent(stockId, stockItem);

            BigDecimal lineDeduct = rate.getQuantityPerServing().multiply(BigDecimal.valueOf(quantity));
            deductTotals.merge(stockId, lineDeduct, BigDecimal::add);

            notes.computeIfAbsent(stockId, k -> new StringBuilder())
                    .append(quantity).append('x').append(menuItemName)
                    .append(sizeCode != null ? "(" + sizeCode + ")" : "")
                    .append("; ");
        }
    }

    private static String rateKey(Long menuItemId, String sizeCode) {
        String size = (sizeCode == null || sizeCode.isBlank()) ? "" : sizeCode.trim().toUpperCase(Locale.ROOT);
        return menuItemId + "|" + size;
    }

    private static String normalizeSizeCode(String sizeCode) {
        if (sizeCode == null || sizeCode.isBlank()) {
            return null;
        }
        return sizeCode.trim().toUpperCase(Locale.ROOT);
    }

    private StockItem requireShopStockItem(Long stockItemId) {
        Long shopId = shopContextService.requireCurrentShopId();
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new RuntimeException("Stock item not found with ID: " + stockItemId));
        if (stockItem.getShop() != null && !Objects.equals(stockItem.getShop().getId(), shopId)) {
            throw new RuntimeException("Stock item not found with ID: " + stockItemId);
        }
        return stockItem;
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

    @Transactional(readOnly = true)
    public StockItemResponse findByBarcode(String rawCode) {
        String code = normalizeBarcode(rawCode);
        if (code == null) {
            throw new RuntimeException("Barcode is required");
        }
        // Support optional CODE:QTY / CODE*QTY suffix from labeled barcodes
        String lookup = code;
        int sep = Math.max(code.lastIndexOf(':'), code.lastIndexOf('*'));
        if (sep > 0 && sep < code.length() - 1) {
            String maybeQty = code.substring(sep + 1);
            try {
                new BigDecimal(maybeQty);
                lookup = code.substring(0, sep);
            } catch (NumberFormatException ignored) {
                // whole string is the barcode
            }
        }
        Long shopId = shopContextService.requireCurrentShopId();
        final String barcodeKey = lookup;
        StockItem stockItem = stockItemRepository.findByShopIdAndBarcodeIgnoreCase(shopId, barcodeKey)
                .orElseThrow(() -> new RuntimeException("No stock item found for barcode: " + barcodeKey));
        return mapToStockItemResponse(stockItem);
    }

    /**
     * Parse optional qty from CODE:QTY or CODE*QTY. Returns null if none.
     */
    public static BigDecimal parseQtyFromScan(String rawCode) {
        String code = normalizeBarcode(rawCode);
        if (code == null) {
            return null;
        }
        int sep = Math.max(code.lastIndexOf(':'), code.lastIndexOf('*'));
        if (sep > 0 && sep < code.length() - 1) {
            try {
                BigDecimal qty = new BigDecimal(code.substring(sep + 1));
                return qty.compareTo(BigDecimal.ZERO) > 0 ? qty : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }
        return barcode.trim();
    }

    private static Long savedShopId(StockItem stockItem) {
        return stockItem.getShop() != null ? stockItem.getShop().getId() : null;
    }

    private void ensureBarcodeUnique(Long shopId, String barcode, Long excludeId) {
        if (barcode == null || shopId == null) {
            return;
        }
        stockItemRepository.findByShopIdAndBarcodeIgnoreCase(shopId, barcode).ifPresent(existing -> {
            if (excludeId == null || !Objects.equals(existing.getId(), excludeId)) {
                throw new RuntimeException("Barcode already used by stock item: " + existing.getNameEn());
            }
        });
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
                .barcode(stockItem.getBarcode())
                .scanPackQty(stockItem.getScanPackQty() != null ? stockItem.getScanPackQty() : BigDecimal.ONE)
                .createdAt(stockItem.getCreatedAt())
                .updatedAt(stockItem.getUpdatedAt())
                .build();
    }
}
