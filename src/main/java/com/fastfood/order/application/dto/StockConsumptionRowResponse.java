package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockConsumptionRowResponse {

    private Long id;
    private Long menuItemId;
    private String menuItemNameEn;
    private Long categoryId;
    private String categoryNameEn;
    private String sizeCode;
    private String sizeNameEn;
    private BigDecimal servingsPerUnit;
    private BigDecimal quantityPerServing;
}
