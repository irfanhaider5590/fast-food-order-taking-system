package com.fastfood.order.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockConsumptionRowRequest {

    @NotNull
    private Long menuItemId;

    /** null / blank = any size */
    private String sizeCode;

    @NotNull
    @DecimalMin(value = "0.0001", message = "servingsPerUnit must be > 0")
    private BigDecimal servingsPerUnit;
}
