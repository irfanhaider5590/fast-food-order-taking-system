package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long categoryId);

    List<MenuItem> findByIsAvailableTrueOrderByDisplayOrderAsc();

    @Query("SELECT mi FROM MenuItem mi WHERE mi.category.id = :categoryId ORDER BY mi.displayOrder ASC")
    List<MenuItem> findByCategoryIdOrderByDisplayOrder(@Param("categoryId") Long categoryId);

    @Query("SELECT mi FROM MenuItem mi JOIN OrderItem oi ON oi.menuItem.id = mi.id " +
           "WHERE oi.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY mi.id ORDER BY SUM(oi.quantity) DESC")
    List<MenuItem> findMostSoldItems(@Param("startDate") java.time.LocalDateTime startDate,
                                     @Param("endDate") java.time.LocalDateTime endDate);
}

