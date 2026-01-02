package com.fastfood.order.application.dto;

import com.fastfood.order.domain.entity.FranchiseInquiry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseInquiryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String city;

    private String province;

    private String country;

    private String investmentRange;

    private String message;

    private FranchiseInquiry.InquiryStatus status;

    private String notes;
}

