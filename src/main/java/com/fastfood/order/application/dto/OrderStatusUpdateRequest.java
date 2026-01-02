package com.fastfood.order.application.dto;

import com.fastfood.order.domain.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    
    @NotNull(message = "Order status is required")
    private Order.OrderStatus orderStatus;
}

