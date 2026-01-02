package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Item name (English) is required")
    private String nameEn;

    private String nameUr;

    private String descriptionEn;

    private String descriptionUr;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private String imageUrl;

    private Boolean isAvailable;

    private Boolean isCombo;

    private Integer displayOrder;

    private List<MenuItemSizeRequest> sizes;

    private List<Long> addOnIds;
}

