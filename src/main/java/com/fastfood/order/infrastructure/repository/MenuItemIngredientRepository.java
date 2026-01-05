package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
    void deleteByMenuItemId(Long menuItemId);
}

