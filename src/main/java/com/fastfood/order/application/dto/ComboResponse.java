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
public class ComboResponse {

    private Long id;
    private String nameEn;
    private String nameUr;
    private String descriptionEn;
    private String descriptionUr;
    private BigDecimal comboPrice;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ComboItemResponse> items;
}

