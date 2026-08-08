package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByCode(String code);
    Optional<Shop> findByIdAndIsActiveTrue(Long id);
}
