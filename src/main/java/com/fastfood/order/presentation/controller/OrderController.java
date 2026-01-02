package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.OrderRequest;
import com.fastfood.order.application.dto.OrderResponse;
import com.fastfood.order.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        // Extract user ID from authentication
        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Implementation to extract user ID
        return 1L; // Placeholder
    }
}

