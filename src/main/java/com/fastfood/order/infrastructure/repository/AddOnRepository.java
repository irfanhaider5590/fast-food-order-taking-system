package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddOnRepository extends JpaRepository<AddOn, Long> {

    List<AddOn> findByIsAvailableTrueOrderByDisplayOrderAsc();
}

