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
public class MenuItemIngredientResponse {

    private Long id;
    private Long stockItemId;
    private String stockItemNameEn;
    private String stockItemNameUr;
    private String stockItemUnit;
    private BigDecimal quantityRequired;
}

