package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemIngredientRequest {

    @NotNull(message = "Stock item ID is required")
    private Long stockItemId;

    @NotNull(message = "Quantity required is mandatory")
    private BigDecimal quantityRequired;
}

