package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String nameEn;
    private String nameUr;
    private String descriptionEn;
    private String descriptionUr;
    private BigDecimal basePrice;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isCombo;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MenuItemSizeResponse> sizes;
    private List<AddOnResponse> availableAddOns;
}

