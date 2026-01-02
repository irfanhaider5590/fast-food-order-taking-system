package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.MenuItemSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemSizeRepository extends JpaRepository<MenuItemSize, Long> {

    List<MenuItemSize> findByMenuItemIdOrderByDisplayOrderAsc(Long menuItemId);

    List<MenuItemSize> findByMenuItemIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long menuItemId);

    void deleteByMenuItemId(Long menuItemId);
}

