package com.fastfood.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {
    
    @NotBlank(message = "Brand name is required")
    private String brandName;
    
    private String brandLogoUrl;
    
    private String contactPhone;
    
    private String contactEmail;
    
    private String address;
}

