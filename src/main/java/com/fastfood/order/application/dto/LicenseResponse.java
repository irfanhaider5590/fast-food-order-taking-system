package com.fastfood.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse {
    @JsonProperty("isActivated")
    private boolean isActivated;
    
    @JsonProperty("isValid")
    private boolean isValid;
    
    private String licenseType;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private long daysRemaining;
    private String message;
    private String machineId;
    
    @JsonProperty("shouldShowWarning")
    private boolean shouldShowWarning;
    
    private String warningMessage;
}

