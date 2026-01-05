package com.fastfood.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "license")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String licenseKey;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    @Column(nullable = false)
    private Integer durationDays;

    @Column
    private LocalDateTime activatedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 255)
    private String machineId;

    @Column(length = 255)
    private String clientName;

    @Column(length = 255)
    private String clientEmail;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum LicenseType {
        TRIAL(30),           // 30 days trial
        MONTHLY(30),         // 1 month
        QUARTERLY(90),       // 3 months
        SEMI_ANNUAL(180),    // 6 months
        ANNUAL(365),         // 1 year
        LIFETIME(Integer.MAX_VALUE); // Lifetime license

        private final int days;

        LicenseType(int days) {
            this.days = days;
        }

        public int getDays() {
            return days;
        }
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        // Lifetime licenses never expire
        if (licenseType == LicenseType.LIFETIME) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isExpired() && activatedAt != null;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

