package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findByCodeAndIsActiveTrueAndValidFromBeforeAndValidUntilAfter(
            String code, LocalDateTime now1, LocalDateTime now2);

    java.util.List<Voucher> findByIsActiveTrue();
}

