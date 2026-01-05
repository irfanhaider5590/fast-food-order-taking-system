package com.fastfood.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_warnings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String warningMessageEn;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String warningMessageUr;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAcknowledged = false;

    @Column
    private LocalDateTime acknowledgedAt;

    @Column(length = 100)
    private String acknowledgedBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

