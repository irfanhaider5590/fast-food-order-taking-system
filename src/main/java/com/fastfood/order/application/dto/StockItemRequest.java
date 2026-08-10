package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockItemRequest {

    @NotBlank(message = "Stock item name (English) is required")
    private String nameEn;

    private String nameUr;

    private String descriptionEn;

    private String descriptionUr;

    private String unit;

    /** Optional on create (defaults to 0). Quantity updates use /adjust or /quantity. */
    private BigDecimal currentQuantity;

    @NotNull(message = "Minimum threshold is required")
    private BigDecimal minThreshold;

    private Boolean isActive;

    /** Optional barcode for scanner restock */
    private String barcode;

    /** Qty added per scan (defaults to 1) */
    private BigDecimal scanPackQty;
}

