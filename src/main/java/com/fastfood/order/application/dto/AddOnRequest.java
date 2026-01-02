package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOnRequest {

    @NotBlank(message = "Add-on name (English) is required")
    private String nameEn;

    private String nameUr;

    private String descriptionEn;

    private String descriptionUr;

    private BigDecimal price;

    private Boolean isAvailable;

    private Integer displayOrder;
}

