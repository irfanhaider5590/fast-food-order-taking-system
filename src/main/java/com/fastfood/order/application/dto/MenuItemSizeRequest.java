package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemSizeRequest {

    private String sizeCode;

    private String sizeNameEn;

    private String sizeNameUr;

    private BigDecimal priceModifier;

    private Boolean isAvailable;

    private Integer displayOrder;
}

