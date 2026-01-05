package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.OrderRequest;
import com.fastfood.order.application.dto.OrderResponse;
import com.fastfood.order.application.dto.OrderStatusUpdateRequest;
import com.fastfood.order.application.service.OrderService;
import com.fastfood.order.presentation.helper.ControllerHelper;
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

/**
 * Controller for order management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication) {
        log.info("GET /api/orders - Fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        log.debug("GET /api/orders - Retrieved {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        log.info("POST /api/orders - Creating order for customer: {}", request.getCustomerName());
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        OrderResponse response = orderService.createOrder(request, userId);
        log.info("POST /api/orders - Order created: {}", response.getOrderNumber());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            Authentication authentication) {
        log.info("PATCH /api/orders/{}/status - Updating status to: {}", orderId, request.getOrderStatus());
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getOrderStatus(), userId);
        return ResponseEntity.ok(response);
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
        log.debug("GET /api/orders/search - Filters: orderNumber={}, customerName={}, branchId={}", 
                orderNumber, customerName, branchId);
        Page<OrderResponse> orders = orderService.searchOrders(
                orderNumber, customerName, customerPhone, startDate, endDate, branchId, pageable);
        return ResponseEntity.ok(orders);
    }
}

