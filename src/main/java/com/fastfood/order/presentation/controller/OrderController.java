package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.OrderRequest;
import com.fastfood.order.application.dto.OrderResponse;
import com.fastfood.order.application.dto.OrderStatusUpdateRequest;
import com.fastfood.order.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication) {
        log.info("GET /api/orders - Fetching all orders");
        try {
            List<OrderResponse> orders = orderService.getAllOrders();
            log.info("GET /api/orders - Successfully retrieved {} orders", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("GET /api/orders - Error fetching orders", e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        log.info("POST /api/orders - Creating new order for customer: {}", request.getCustomerName());
        try {
            // Extract user ID from authentication
            Long userId = getUserIdFromAuthentication(authentication);
            log.debug("POST /api/orders - User ID: {}, Order items count: {}", userId, request.getItems().size());
            OrderResponse response = orderService.createOrder(request, userId);
            log.info("POST /api/orders - Order created successfully: {}", response.getOrderNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /api/orders - Error creating order for customer: {}", request.getCustomerName(), e);
            throw e;
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            Authentication authentication) {
        log.info("PATCH /api/orders/{}/status - Updating order status to: {}", orderId, request.getOrderStatus());
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            OrderResponse response = orderService.updateOrderStatus(orderId, request.getOrderStatus(), userId);
            log.info("PATCH /api/orders/{}/status - Order status updated successfully", orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PATCH /api/orders/{}/status - Error updating order status", orderId, e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderResponse>> searchOrders(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Long branchId,
            Pageable pageable,
            Authentication authentication) {
        log.info("GET /api/orders/search - Searching orders with filters: orderNumber={}, customerName={}, customerPhone={}, startDate={}, endDate={}, branchId={}",
                orderNumber, customerName, customerPhone, startDate, endDate, branchId);
        try {
            Page<OrderResponse> orders = orderService.searchOrders(orderNumber, customerName, customerPhone, startDate, endDate, branchId, pageable);
            log.info("GET /api/orders/search - Found {} orders", orders.getTotalElements());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("GET /api/orders/search - Error searching orders", e);
            throw e;
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("Extracting user ID from authentication: {}", authentication.getName());
            // Implementation to extract user ID
            return 1L; // Placeholder
        }
        log.warn("Authentication is null or not authenticated, using default user ID: 1");
        return 1L; // Placeholder
    }
}

