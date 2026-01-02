package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryRequest {

    @NotBlank(message = "Category name (English) is required")
    private String nameEn;

    private String nameUr;

    private String descriptionEn;

    private String descriptionUr;

    private Integer displayOrder;

    private String imageUrl;

    private Boolean isActive;
}

