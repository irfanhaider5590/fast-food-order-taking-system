package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByLicenseKey(String licenseKey);
    Optional<License> findByMachineIdAndIsActiveTrue(String machineId);
    List<License> findByIsActiveTrue();
    List<License> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);
}

