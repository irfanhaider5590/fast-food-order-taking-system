package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemResponse {

    private Long id;
    private String nameEn;
    private String nameUr;
    private String descriptionEn;
    private String descriptionUr;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal minThreshold;
    private Boolean isActive;
    private Boolean isLowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

