package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {

    List<Combo> findByIsAvailableTrueOrderByDisplayOrderAsc();
}

