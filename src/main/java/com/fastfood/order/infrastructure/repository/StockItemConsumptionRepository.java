package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.StockItemConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockItemConsumptionRepository extends JpaRepository<StockItemConsumption, Long> {

    List<StockItemConsumption> findByStockItemIdOrderByIdAsc(Long stockItemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM StockItemConsumption c WHERE c.stockItem.id = :stockItemId")
    void deleteByStockItemId(@Param("stockItemId") Long stockItemId);

    @Query("""
            SELECT c FROM StockItemConsumption c
            JOIN FETCH c.stockItem s
            JOIN FETCH c.menuItem m
            WHERE s.shop.id = :shopId AND s.isActive = true
            """)
    List<StockItemConsumption> findActiveByShopId(@Param("shopId") Long shopId);

    @Query("""
            SELECT c FROM StockItemConsumption c
            JOIN FETCH c.stockItem s
            JOIN FETCH c.menuItem m
            LEFT JOIN FETCH m.category
            WHERE c.stockItem.id = :stockItemId
            ORDER BY c.id ASC
            """)
    List<StockItemConsumption> findDetailedByStockItemId(@Param("stockItemId") Long stockItemId);
}
