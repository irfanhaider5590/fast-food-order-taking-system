package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByStockItemIdOrderByCreatedAtDesc(Long stockItemId);
    List<StockTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    List<StockTransaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

