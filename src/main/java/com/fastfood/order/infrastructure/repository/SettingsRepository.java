package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findFirstByOrderByIdAsc();
}

