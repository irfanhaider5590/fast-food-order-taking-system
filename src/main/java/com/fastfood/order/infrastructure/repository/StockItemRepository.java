package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    List<StockItem> findByIsActiveTrueOrderByNameEnAsc();
    List<StockItem> findByCurrentQuantityLessThanEqualOrderByCurrentQuantityAsc(BigDecimal threshold);
}

