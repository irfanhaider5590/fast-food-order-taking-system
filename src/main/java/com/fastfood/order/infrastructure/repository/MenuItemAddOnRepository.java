package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.MenuItemAddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemAddOnRepository extends JpaRepository<MenuItemAddOn, Long> {

    List<MenuItemAddOn> findByMenuItemId(Long menuItemId);

    void deleteByMenuItemId(Long menuItemId);
}

