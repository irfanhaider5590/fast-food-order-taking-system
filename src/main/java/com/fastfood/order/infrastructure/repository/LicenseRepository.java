package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByLicenseKey(String licenseKey);

    /**
     * Prefer lifetime (null expiry) then latest expiry. Caller should take the first row.
     */
    List<License> findByMachineIdAndIsActiveTrueOrderByExpiresAtDesc(String machineId);

    List<License> findByIsActiveTrue();

    List<License> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);

    @Modifying
    @Query("UPDATE License l SET l.isActive = false, l.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE l.machineId = :machineId AND l.isActive = true AND l.id <> :keepId")
    int deactivateOtherActiveLicenses(@Param("machineId") String machineId, @Param("keepId") Long keepId);
}
