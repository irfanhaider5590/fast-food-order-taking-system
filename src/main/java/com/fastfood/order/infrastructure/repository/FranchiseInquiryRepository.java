package com.fastfood.order.infrastructure.repository;

import com.fastfood.order.domain.entity.FranchiseInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInquiryRepository extends JpaRepository<FranchiseInquiry, Long> {

    List<FranchiseInquiry> findByStatusOrderByCreatedAtDesc(FranchiseInquiry.InquiryStatus status);
}

