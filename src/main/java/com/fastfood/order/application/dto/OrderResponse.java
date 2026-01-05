package com.fastfood.order.application.dto;

import com.fastfood.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long branchId;
    private String branchName;
    private Order.OrderType orderType;
    private String tableNumber;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private Order.OrderStatus orderStatus;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String voucherCode;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime completedAt;
    private List<OrderItemResponse> items;
    private List<StockWarningResponse> stockWarnings; // Warnings for low stock items
}

