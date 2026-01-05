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
public class StockWarningResponse {

    private Long id;
    private Long stockItemId;
    private String stockItemNameEn;
    private String stockItemNameUr;
    private String warningMessageEn;
    private String warningMessageUr;
    private BigDecimal currentQuantity;
    private BigDecimal thresholdQuantity;
    private Boolean isAcknowledged;
    private LocalDateTime createdAt;
}

