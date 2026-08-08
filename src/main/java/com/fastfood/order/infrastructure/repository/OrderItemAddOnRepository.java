package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.OrderItemAddOn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemAddOnRepository extends JpaRepository<OrderItemAddOn, Long> {
    List<OrderItemAddOn> findByOrderItemId(Long orderItemId);
}
