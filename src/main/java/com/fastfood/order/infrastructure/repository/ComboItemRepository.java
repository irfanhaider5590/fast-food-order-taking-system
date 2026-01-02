package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Long> {

    List<ComboItem> findByComboIdOrderByDisplayOrderAsc(Long comboId);

    void deleteByComboId(Long comboId);
}

