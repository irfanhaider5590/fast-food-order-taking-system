package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionCatalogResponse {

    @Builder.Default
    private List<ConsumptionCatalogCategory> categories = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionCatalogCategory {
        private Long id;
        private String nameEn;
        @Builder.Default
        private List<ConsumptionCatalogItem> items = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionCatalogItem {
        private Long id;
        private String nameEn;
        private Long categoryId;
        @Builder.Default
        private List<ConsumptionCatalogSize> sizes = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionCatalogSize {
        private String sizeCode;
        private String sizeNameEn;
    }
}
