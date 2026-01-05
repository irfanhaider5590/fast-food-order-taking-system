package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.OrderRequest;
import com.fastfood.order.application.dto.OrderResponse;
import com.fastfood.order.application.dto.OrderItemResponse;
import com.fastfood.order.application.mapper.OrderMapper;
import com.fastfood.order.application.mapper.OrderItemMapper;
import com.fastfood.order.domain.entity.*;
import com.fastfood.order.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final MenuItemRepository menuItemRepository;
    private final ComboRepository comboRepository;
    private final VoucherRepository voucherRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddOnRepository addOnRepository;
    private final ReceiptPrintService receiptPrintService;
    private final StockManagementService stockManagementService;
    private final StockWarningService stockWarningService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        log.info("Creating order for branch ID: {}", request.getBranchId());
        
        Branch branch = findBranchById(request.getBranchId());
        String orderNumber = generateOrderNumber();
        Order order = buildOrder(request, branch, orderNumber);
        
        List<OrderItem> orderItems = buildOrderItems(request.getItems(), order);
        BigDecimal subtotal = calculateSubtotal(orderItems);
        order.setSubtotal(subtotal);
        
        BigDecimal discountAmount = applyVoucherDiscount(request.getVoucherCode(), subtotal, order);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(subtotal.subtract(discountAmount));
        
        Order savedOrder = orderRepository.save(order);
        saveOrderItems(savedOrder, orderItems);
        
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        
        // Deduct stock for order items
        try {
            stockManagementService.deductStockForOrder(savedOrder);
        } catch (Exception e) {
            log.error("Error deducting stock for order: {}", savedOrder.getOrderNumber(), e);
            // Don't fail the order if stock deduction fails
        }
        
        // Check for stock warnings after deduction
        List<com.fastfood.order.application.dto.StockWarningResponse> stockWarnings = null;
        try {
            stockWarnings = stockWarningService.checkStockWarningsOnOrder();
            if (!stockWarnings.isEmpty()) {
                log.warn("Low stock warnings detected after order placement: {}", stockWarnings.size());
            }
        } catch (Exception e) {
            log.error("Error checking stock warnings for order: {}", savedOrder.getOrderNumber(), e);
            // Don't fail the order if warning check fails
        }
        
        OrderResponse response = mapToOrderResponse(savedOrder);
        response.setStockWarnings(stockWarnings);
        
        // Print receipt if enabled
        try {
            receiptPrintService.printReceipt(response);
        } catch (Exception e) {
            log.error("Error printing receipt for order: {}", savedOrder.getOrderNumber(), e);
            // Don't fail the order if printing fails
        }
        
        return response;
    }

    // Private helper methods
    
    private Branch findBranchById(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               "-" + String.format("%04d", (int)(Math.random() * 10000));
    }

    private Order buildOrder(OrderRequest request, Branch branch, String orderNumber) {
        return Order.builder()
                .orderNumber(orderNumber)
                .branch(branch)
                .orderType(request.getOrderType())
                .tableNumber(request.getTableNumber())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .orderStatus(Order.OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .notes(request.getNotes())
                .build();
    }

    private List<OrderItem> buildOrderItems(List<com.fastfood.order.application.dto.OrderItemRequest> itemRequests, Order order) {
        return itemRequests.stream()
                .map(itemRequest -> createOrderItem(itemRequest, order))
                .collect(Collectors.toList());
    }

    private OrderItem createOrderItem(com.fastfood.order.application.dto.OrderItemRequest itemRequest, Order order) {
        ItemDetails itemDetails = getItemDetails(itemRequest);
        BigDecimal unitPrice = itemDetails.getUnitPrice();
        
        // Apply size modifier if applicable
        if (itemRequest.getSizeCode() != null && itemDetails.getMenuItem() != null) {
            // Size modifier logic would go here if needed
        }
        
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
        
        return OrderItem.builder()
                .order(order)
                .menuItem(itemDetails.getMenuItem())
                .combo(itemDetails.getCombo())
                .itemNameEn(itemDetails.getItemNameEn())
                .itemNameUr(itemDetails.getItemNameUr())
                .sizeCode(itemRequest.getSizeCode())
                .quantity(itemRequest.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .notes(itemRequest.getNotes())
                .build();
    }

    private ItemDetails getItemDetails(com.fastfood.order.application.dto.OrderItemRequest itemRequest) {
        if (itemRequest.getMenuItemId() != null) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));
            return new ItemDetails(
                    menuItem.getNameEn(),
                    menuItem.getNameUr(),
                    menuItem.getBasePrice(),
                    menuItem,
                    null
            );
        } else if (itemRequest.getComboId() != null) {
            Combo combo = comboRepository.findById(itemRequest.getComboId())
                    .orElseThrow(() -> new RuntimeException("Combo not found"));
            return new ItemDetails(
                    combo.getNameEn(),
                    combo.getNameUr(),
                    combo.getComboPrice(),
                    null,
                    combo
            );
        }
        throw new RuntimeException("Either menuItemId or comboId must be provided");
    }

    private BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal applyVoucherDiscount(String voucherCode, BigDecimal subtotal, Order order) {
        if (voucherCode == null || voucherCode.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Voucher voucher = findValidVoucher(voucherCode);
        if (voucher == null) {
            log.warn("Invalid or expired voucher code: {}", voucherCode);
            return BigDecimal.ZERO;
        }
        
        if (voucher.getUsedCount() >= (voucher.getUsageLimit() != null ? voucher.getUsageLimit() : Integer.MAX_VALUE)) {
            log.warn("Voucher usage limit exceeded: {}", voucherCode);
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = calculateDiscount(subtotal, voucher);
        order.setVoucher(voucher);
        order.setVoucherCode(voucher.getCode());
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        
        return discountAmount;
    }

    private Voucher findValidVoucher(String voucherCode) {
        return voucherRepository.findByCodeAndIsActiveTrueAndValidFromBeforeAndValidUntilAfter(
                voucherCode, LocalDateTime.now(), LocalDateTime.now())
                .orElse(null);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, Voucher voucher) {
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            BigDecimal discount = subtotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                return voucher.getMaxDiscountAmount();
            }
            return discount;
        } else {
            return voucher.getDiscountValue();
        }
    }

    private void saveOrderItems(Order savedOrder, List<OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            orderItem.setOrder(savedOrder);
            orderItemRepository.save(orderItem);
        });
    }

    public List<OrderResponse> getAllOrders() {
        log.info("Fetching pending orders from database");
        try {
            // Only fetch PENDING orders for recent orders screen
            List<Order> orders = orderRepository.findByOrderStatusOrderByOrderDateDesc(Order.OrderStatus.PENDING);
            log.info("Found {} pending orders in database", orders.size());
            
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::mapToOrderResponse)
                    .collect(Collectors.toList());
            
            log.debug("Mapped {} orders to response DTOs", orderResponses.size());
            return orderResponses;
        } catch (Exception e) {
            log.error("Error fetching orders from database", e);
            throw e;
        }
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus, Long userId) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        Order.OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        
        // Set completedAt timestamp if status is COMPLETED
        if (newStatus == Order.OrderStatus.COMPLETED && oldStatus != Order.OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", orderId, oldStatus, newStatus);
        
        return mapToOrderResponse(updatedOrder);
    }

    public Page<OrderResponse> searchOrders(String orderNumber, String customerName, String customerPhone,
                                           LocalDate startDate, LocalDate endDate, Long branchId, Pageable pageable) {
        log.info("Searching orders with filters: orderNumber={}, customerName={}, customerPhone={}, startDate={}, endDate={}, branchId={}",
                orderNumber, customerName, customerPhone, startDate, endDate, branchId);
        
        Specification<Order> spec = Specification.where(null);
        boolean hasFilters = false;
        
        if (orderNumber != null && !orderNumber.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("orderNumber")), "%" + orderNumber.toLowerCase() + "%"));
            hasFilters = true;
        }
        
        if (customerName != null && !customerName.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("customerName")), "%" + customerName.toLowerCase() + "%"));
            hasFilters = true;
        }
        
        if (customerPhone != null && !customerPhone.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("customerPhone"), "%" + customerPhone + "%"));
            hasFilters = true;
        }
        
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("orderDate"), startDateTime));
            hasFilters = true;
        }
        
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("orderDate"), endDateTime));
            hasFilters = true;
        }
        
        if (branchId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("branch").get("id"), branchId));
        }
        
        // Always sort by orderDate desc first
        Sort sort = Sort.by(Sort.Order.desc("orderDate"));
        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        
        Page<Order> orders = orderRepository.findAll(spec, finalPageable);
        
        // If no filters applied, sort results to put PENDING orders first, then by date desc
        if (!hasFilters) {
            List<OrderResponse> content = orders.getContent().stream()
                    .map(this::mapToOrderResponse)
                    .sorted((a, b) -> {
                        // PENDING orders first
                        boolean aIsPending = a.getOrderStatus() == Order.OrderStatus.PENDING;
                        boolean bIsPending = b.getOrderStatus() == Order.OrderStatus.PENDING;
                        if (aIsPending && !bIsPending) return -1;
                        if (!aIsPending && bIsPending) return 1;
                        // Then by date desc
                        LocalDateTime dateA = a.getOrderDate() != null ? a.getOrderDate() : LocalDateTime.MIN;
                        LocalDateTime dateB = b.getOrderDate() != null ? b.getOrderDate() : LocalDateTime.MIN;
                        return dateB.compareTo(dateA);
                    })
                    .collect(Collectors.toList());
            
            return new org.springframework.data.domain.PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
        }
        
        return orders.map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        // Use OrderMapper to map Order entity to OrderResponse DTO
        OrderResponse response = orderMapper.toResponse(order);
        
        // Load and map order items using OrderItemMapper
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        return response;
    }

    // Helper class for item details
    private static class ItemDetails {
        private final String itemNameEn;
        private final String itemNameUr;
        private final BigDecimal unitPrice;
        private final MenuItem menuItem;
        private final Combo combo;

        public ItemDetails(String itemNameEn, String itemNameUr, BigDecimal unitPrice, MenuItem menuItem, Combo combo) {
            this.itemNameEn = itemNameEn;
            this.itemNameUr = itemNameUr;
            this.unitPrice = unitPrice;
            this.menuItem = menuItem;
            this.combo = combo;
        }

        public String getItemNameEn() { return itemNameEn; }
        public String getItemNameUr() { return itemNameUr; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public MenuItem getMenuItem() { return menuItem; }
        public Combo getCombo() { return combo; }
    }
}
