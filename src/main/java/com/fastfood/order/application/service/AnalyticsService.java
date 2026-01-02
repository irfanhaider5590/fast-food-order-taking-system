package com.fastfood.order.application.service;

import com.fastfood.order.application.dto.SalesAnalyticsResponse;
import com.fastfood.order.domain.entity.Order;
import com.fastfood.order.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSalesAnalytics(Long branchId, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        
        // Current month
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        SalesAnalyticsResponse.SalesSummary currentMonth = calculateSummary(
                branchId != null ? 
                        orderRepository.findOrdersByBranchAndDateRange(branchId, currentMonthStart, now) :
                        orderRepository.findOrdersByDateRange(currentMonthStart, now)
        );

        // Last 3 months
        LocalDateTime threeMonthsAgo = now.minusMonths(3).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        SalesAnalyticsResponse.SalesSummary lastThreeMonths = calculateSummary(
                branchId != null ?
                        orderRepository.findOrdersByBranchAndDateRange(branchId, threeMonthsAgo, now) :
                        orderRepository.findOrdersByDateRange(threeMonthsAgo, now)
        );

        // Last 12 months
        LocalDateTime twelveMonthsAgo = now.minusMonths(12).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        SalesAnalyticsResponse.SalesSummary lastTwelveMonths = calculateSummary(
                branchId != null ?
                        orderRepository.findOrdersByBranchAndDateRange(branchId, twelveMonthsAgo, now) :
                        orderRepository.findOrdersByDateRange(twelveMonthsAgo, now)
        );

        // Monthly sales data
        List<SalesAnalyticsResponse.MonthlySalesData> monthlySales = calculateMonthlySales(branchId, twelveMonthsAgo, now);

        return SalesAnalyticsResponse.builder()
                .currentMonth(currentMonth)
                .lastThreeMonths(lastThreeMonths)
                .lastTwelveMonths(lastTwelveMonths)
                .monthlySales(monthlySales)
                .build();
    }

    private SalesAnalyticsResponse.SalesSummary calculateSummary(List<Order> orders) {
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalOrders = (long) orders.size();

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalSales.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return SalesAnalyticsResponse.SalesSummary.builder()
                .totalSales(totalSales)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    private List<SalesAnalyticsResponse.MonthlySalesData> calculateMonthlySales(Long branchId, LocalDateTime start, LocalDateTime end) {
        List<SalesAnalyticsResponse.MonthlySalesData> monthlyData = new ArrayList<>();
        
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            LocalDateTime monthStart = current.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = current.withDayOfMonth(current.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);

            List<Order> monthOrders = branchId != null ?
                    orderRepository.findOrdersByBranchAndDateRange(branchId, monthStart, monthEnd) :
                    orderRepository.findOrdersByDateRange(monthStart, monthEnd);

            BigDecimal monthSales = monthOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyData.add(SalesAnalyticsResponse.MonthlySalesData.builder()
                    .month(current.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")))
                    .sales(monthSales)
                    .orders((long) monthOrders.size())
                    .build());

            current = current.plusMonths(1);
        }

        return monthlyData;
    }
}

