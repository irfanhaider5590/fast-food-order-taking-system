package com.fastfood.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsResponse {

    private SalesSummary currentMonth;
    private SalesSummary lastThreeMonths;
    private SalesSummary lastTwelveMonths;
    private List<MonthlySalesData> monthlySales;
    private List<CategorySalesData> categorySales;
    private List<PopularItemData> popularItems;
    private List<ComboPerformanceData> comboPerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesSummary {
        private BigDecimal totalSales;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySalesData {
        private String month;
        private BigDecimal sales;
        private Long orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySalesData {
        private String categoryName;
        private BigDecimal sales;
        private Long orders;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularItemData {
        private Long itemId;
        private String itemName;
        private Long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComboPerformanceData {
        private Long comboId;
        private String comboName;
        private Long orders;
        private BigDecimal revenue;
    }
}
