package com.fastfood.order.application.dto;

import com.fastfood.order.domain.entity.FranchiseInquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseInquiryResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String province;
    private String country;
    private String investmentRange;
    private String message;
    private FranchiseInquiry.InquiryStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

