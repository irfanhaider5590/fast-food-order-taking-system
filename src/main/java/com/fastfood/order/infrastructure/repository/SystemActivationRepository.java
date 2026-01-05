package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.SystemActivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemActivationRepository extends JpaRepository<SystemActivation, Long> {
    Optional<SystemActivation> findByMachineId(String machineId);
    Optional<SystemActivation> findByMachineIdAndIsActiveTrue(String machineId);
}

