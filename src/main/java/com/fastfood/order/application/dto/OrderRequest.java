package com.fastfood.order.application.dto;

import com.fastfood.order.domain.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Order type is required")
    private Order.OrderType orderType;

    private String tableNumber;

    private String customerName;

    private String customerPhone;

    private String deliveryAddress;

    @NotNull(message = "Payment method is required")
    private Order.PaymentMethod paymentMethod;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;

    private String voucherCode;

    private String notes;
}

