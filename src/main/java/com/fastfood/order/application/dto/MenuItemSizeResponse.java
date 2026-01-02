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
public class MenuItemSizeResponse {

    private Long id;
    private String sizeCode;
    private String sizeNameEn;
    private String sizeNameUr;
    private BigDecimal priceModifier;
    private Boolean isAvailable;
    private Integer displayOrder;
}

