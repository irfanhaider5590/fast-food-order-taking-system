package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboRequest {

    @NotBlank(message = "Combo name (English) is required")
    private String nameEn;

    private String nameUr;

    private String descriptionEn;

    private String descriptionUr;

    @NotNull(message = "Combo price is required")
    private BigDecimal comboPrice;

    private String imageUrl;

    private Boolean isAvailable;

    private Integer displayOrder;

    @NotEmpty(message = "Combo items are required")
    private List<ComboItemRequest> items;
}

