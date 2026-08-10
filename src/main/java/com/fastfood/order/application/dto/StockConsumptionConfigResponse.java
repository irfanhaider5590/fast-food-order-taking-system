package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockConsumptionConfigResponse {

    private Long stockItemId;
    private String stockItemNameEn;
    private String stockItemNameUr;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal minThreshold;

    @Builder.Default
    private List<StockConsumptionRowResponse> rows = new ArrayList<>();
}
