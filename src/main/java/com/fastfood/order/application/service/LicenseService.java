package com.fastfood.order.application.service;

import com.fastfood.order.domain.entity.License;
import com.fastfood.order.domain.entity.SystemActivation;
import com.fastfood.order.infrastructure.repository.LicenseRepository;
import com.fastfood.order.infrastructure.repository.SystemActivationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final SystemActivationRepository systemActivationRepository;

    /**
     * Get or create server ID (unique identifier for this server instance)
     * This ID is shared by all users connecting to the same server
     * Stored in database to persist across restarts
     */
    @Transactional
    public String getMachineId() {
        // First, try to get server ID from database (persistent storage)
        Optional<SystemActivation> existingActivation = systemActivationRepository.findAll().stream()
                .filter(act -> act.getMachineId() != null && !act.getMachineId().isEmpty())
                .findFirst();
        
        if (existingActivation.isPresent()) {
            String serverId = existingActivation.get().getMachineId();
            log.debug("Using existing server ID from database: {}", serverId);
            return serverId;
        }
        
        // If not in database, generate server ID based on server properties
        // This ensures all users on the same server share the same ID
        String hostName = System.getenv("COMPUTERNAME"); // Windows
        if (hostName == null) {
            hostName = System.getenv("HOSTNAME"); // Linux/Mac
        }
        if (hostName == null) {
            try {
                hostName = java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                hostName = "unknown-host";
            }
        }
        
        // Get server IP address
        String serverIp = "unknown-ip";
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            serverIp = localHost.getHostAddress();
        } catch (Exception e) {
            log.warn("Could not determine server IP address", e);
        }
        
        // Create a unique server ID based on server hostname and IP
        // This ensures the same server always gets the same ID
        String uniqueString = (hostName + serverIp).replaceAll("[^a-zA-Z0-9]", "");
        String serverId = UUID.nameUUIDFromBytes(uniqueString.getBytes()).toString();
        
        log.info("Generated new server ID: {} (hostname: {}, IP: {})", serverId, hostName, serverIp);
        log.info("Note: This server ID will be shared by all users connecting to this server instance");
        
        // Store in system properties for this session (cache)
        System.setProperty("machine.id", serverId);
        
        return serverId;
    }

    /**
     * Check if system is activated (first-time deployment check)
     */
    @Transactional(readOnly = true)
    public boolean isSystemActivated() {
        String machineId = getMachineId();
        Optional<SystemActivation> activation = systemActivationRepository.findByMachineId(machineId);
        return activation.isPresent();
    }

    /**
     * Activate system for first time or extend existing license
     * Uses server ID (shared by all users on the same server)
     */
    @Transactional
    public SystemActivation activateSystem(String licenseKey) {
        String serverId = getMachineId();
        
        // Validate and activate/extend license (this handles both new activation and extension)
        validateAndActivateLicense(licenseKey, serverId);
        
        // Check if system activation record exists for this server
        Optional<SystemActivation> existing = systemActivationRepository.findByMachineId(serverId);
        if (existing.isPresent()) {
            // Update existing activation record
            SystemActivation activation = existing.get();
            activation.setIsActive(true);
            activation.setLastCheckDate(LocalDateTime.now());
            activation.setLicenseKey(licenseKey);
            SystemActivation saved = systemActivationRepository.save(activation);
            log.info("License extended/updated successfully with license: {} for server: {}", licenseKey, serverId);
            return saved;
        }
        
        // First-time activation - create system activation record with server ID
        SystemActivation activation = SystemActivation.builder()
                .machineId(serverId) // Server ID, shared by all users
                .licenseKey(licenseKey)
                .firstActivationDate(LocalDateTime.now())
                .isActive(true)
                .lastCheckDate(LocalDateTime.now())
                .build();
        
        SystemActivation saved = systemActivationRepository.save(activation);
        log.info("System activated successfully with license: {} for server: {} (shared by all users)", licenseKey, serverId);
        
        return saved;
    }

    /**
     * Validate license and check if system should be allowed to run
     * Uses server ID (shared by all users on the same server)
     */
    @Transactional(readOnly = true)
    public boolean isLicenseValid() {
        String serverId = getMachineId();
        
        // Check system activation for this server
        Optional<SystemActivation> activation = systemActivationRepository.findByMachineIdAndIsActiveTrue(serverId);
        if (activation.isEmpty()) {
            log.warn("System not activated - first time deployment required for server: {}", serverId);
            return false;
        }

        // Check license for this server
        Optional<License> licenseOpt = licenseRepository.findByMachineIdAndIsActiveTrue(serverId);
        if (licenseOpt.isEmpty()) {
            log.warn("No active license found for server: {}", serverId);
            return false;
        }

        License license = licenseOpt.get();
        
        // Update last check date
        activation.get().setLastCheckDate(LocalDateTime.now());
        systemActivationRepository.save(activation.get());
        
        if (!license.isValid()) {
            log.error("License expired or invalid. Expires at: {}, Active: {}", license.getExpiresAt(), license.getIsActive());
            return false;
        }

        return true;
    }

    /**
     * Validate and activate license
     * If a license already exists for this server, extends the expiry date instead of replacing
     * Uses server ID (shared by all users on the same server)
     */
    @Transactional
    public License validateAndActivateLicense(String licenseKey, String serverId) {
        License newLicense = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new RuntimeException("Invalid license key"));

        // Check if license is already activated on another server
        if (newLicense.getMachineId() != null && !newLicense.getMachineId().equals(serverId)) {
            throw new RuntimeException("License is already activated on another server");
        }

        // Check if new license is already activated on another server
        if (newLicense.getActivatedAt() != null && newLicense.getMachineId() != null && !newLicense.getMachineId().equals(serverId)) {
            throw new RuntimeException("License is already activated on another server");
        }
        
        // Check if there's an existing active license for this server
        Optional<License> existingLicenseOpt = licenseRepository.findByMachineIdAndIsActiveTrue(serverId);
        
        if (existingLicenseOpt.isPresent()) {
            // Extend existing license
            License existingLicense = existingLicenseOpt.get();
            
            // If new license is already activated on this server, just return existing
            if (newLicense.getActivatedAt() != null && newLicense.getMachineId() != null && newLicense.getMachineId().equals(serverId)) {
                log.info("License {} is already activated on this server, extending existing license", licenseKey);
                // Still extend the existing license
            }
            
            // Calculate new expiry date by adding days to existing expiry (or current date if expired)
            LocalDateTime currentExpiry = existingLicense.getExpiresAt();
            LocalDateTime baseDate = (currentExpiry != null && currentExpiry.isAfter(LocalDateTime.now())) 
                    ? currentExpiry 
                    : LocalDateTime.now();
            
            if (newLicense.getLicenseType() == License.LicenseType.LIFETIME) {
                // Lifetime license - set to never expire
                existingLicense.setExpiresAt(null);
                existingLicense.setLicenseType(License.LicenseType.LIFETIME);
            } else {
                // Add new license days to existing expiry
                existingLicense.setExpiresAt(baseDate.plusDays(newLicense.getDurationDays()));
            }
            
            existingLicense.setIsActive(true);
            existingLicense.setUpdatedAt(LocalDateTime.now());
            
            // Mark new license as used (but keep it active for tracking)
            newLicense.setMachineId(serverId);
            newLicense.setActivatedAt(LocalDateTime.now());
            newLicense.setIsActive(true);
            // Set expiry same as extended license for consistency
            if (newLicense.getLicenseType() == License.LicenseType.LIFETIME) {
                newLicense.setExpiresAt(null);
            } else {
                newLicense.setExpiresAt(existingLicense.getExpiresAt());
            }
            
            licenseRepository.save(newLicense);
            License saved = licenseRepository.save(existingLicense);
            
            log.info("License extended: {} days added to existing license for server: {}, new expiry: {}", 
                    newLicense.getDurationDays(), serverId, saved.getExpiresAt());
            
            return saved;
        } else {
            // First time activation - activate the new license
            if (newLicense.getActivatedAt() != null && newLicense.getMachineId() != null && !newLicense.getMachineId().equals(serverId)) {
                throw new RuntimeException("License is already activated on another server");
            }

            // Activate license for this server (shared by all users)
            newLicense.setMachineId(serverId);
            newLicense.setActivatedAt(LocalDateTime.now());
            // Lifetime licenses don't have expiration date
            if (newLicense.getLicenseType() == License.LicenseType.LIFETIME) {
                newLicense.setExpiresAt(null); // Never expires
            } else {
                newLicense.setExpiresAt(LocalDateTime.now().plusDays(newLicense.getDurationDays()));
            }
            newLicense.setIsActive(true);

            License saved = licenseRepository.save(newLicense);
            log.info("License activated: {} for server: {} (shared by all users), expires at: {}", licenseKey, serverId, saved.getExpiresAt());
            
            return saved;
        }
    }

    /**
     * Get license status
     * Uses server ID (shared by all users on the same server)
     */
    @Transactional(readOnly = true)
    public LicenseStatus getLicenseStatus() {
        String serverId = getMachineId();
        Optional<SystemActivation> activation = systemActivationRepository.findByMachineId(serverId);
        
        if (activation.isEmpty()) {
            return LicenseStatus.builder()
                    .isActivated(false)
                    .isValid(false)
                    .message("System not activated. Please activate with a valid license key.")
                    .machineId(serverId)
                    .build();
        }

        Optional<License> licenseOpt = licenseRepository.findByMachineIdAndIsActiveTrue(serverId);
        if (licenseOpt.isEmpty()) {
            return LicenseStatus.builder()
                    .isActivated(true)
                    .isValid(false)
                    .message("No active license found. Please activate with a valid license key.")
                    .machineId(serverId)
                    .build();
        }

        License license = licenseOpt.get();
        
        // Check if license is expired
        boolean isExpired = license.isExpired();
        boolean isActive = license.getIsActive() != null && license.getIsActive();
        boolean hasActivatedAt = license.getActivatedAt() != null;
        
        // License is valid if: active, not expired, and has activation date
        boolean isValid = isActive && !isExpired && hasActivatedAt;
        
        // Log for debugging
        log.info("License validation - isActive: {}, isExpired: {}, hasActivatedAt: {}, isValid: {}", 
                isActive, isExpired, hasActivatedAt, isValid);
        
        String message;
        if (!isValid) {
            if (isExpired) {
                message = String.format("License expired on %s. Please renew your license.", license.getExpiresAt());
            } else if (!isActive) {
                message = "License is inactive. Please contact support.";
            } else if (!hasActivatedAt) {
                message = "License not activated yet. Please activate with a valid license key.";
            } else {
                message = "License is invalid. Please contact support.";
            }
        } else {
            long daysRemaining = license.getExpiresAt() != null 
                    ? java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), license.getExpiresAt())
                    : Integer.MAX_VALUE;
            if (license.getLicenseType() == License.LicenseType.LIFETIME) {
                message = "License valid. Lifetime license - never expires.";
            } else {
                message = String.format("License valid. %d days remaining. Expires on %s", daysRemaining, license.getExpiresAt());
            }
        }

        long daysRemaining = isValid && license.getExpiresAt() != null 
                ? java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), license.getExpiresAt())
                : 0;
        
        // Log days remaining for debugging
        log.info("License days remaining: {}, expiresAt: {}, now: {}", 
                daysRemaining, license.getExpiresAt(), LocalDateTime.now());
        
        // Check if warning should be shown (15, 10, 5, 4, 3, 2, 1 days remaining)
        boolean shouldShowWarning = false;
        String warningMessage = null;
        
        if (isValid && license.getExpiresAt() != null && license.getLicenseType() != License.LicenseType.LIFETIME) {
            if (daysRemaining <= 15 && daysRemaining >= 0) { // Changed > 0 to >= 0 to include 0 days
                // Check if it's one of the warning days
                if (daysRemaining == 15 || daysRemaining == 10 || daysRemaining == 5 || 
                    daysRemaining == 4 || daysRemaining == 3 || daysRemaining == 2 || 
                    daysRemaining == 1 || daysRemaining == 0) {
                    shouldShowWarning = true;
                    if (daysRemaining == 0) {
                        warningMessage = "Warning: Your license expires today! Please renew immediately.";
                    } else {
                        warningMessage = String.format("Warning: Your license will expire in %d day(s). Please renew your license before expiration.", daysRemaining);
                    }
                    log.info("License warning triggered - daysRemaining: {}, warningMessage: {}", daysRemaining, warningMessage);
                }
            }
        }

        LicenseStatus status = LicenseStatus.builder()
                .isActivated(hasActivatedAt) // Set based on whether license has activation date
                .isValid(isValid)
                .licenseType(license.getLicenseType().name())
                .activatedAt(license.getActivatedAt())
                .expiresAt(license.getExpiresAt())
                .daysRemaining(daysRemaining)
                .message(message)
                .shouldShowWarning(shouldShowWarning)
                .warningMessage(warningMessage)
                .machineId(serverId) // Server ID, shared by all users
                .build();
        
        log.info("Returning license status - isValid: {}, isActivated: {}, daysRemaining: {}, message: {}", 
                status.isValid(), status.isActivated(), status.getDaysRemaining(), status.getMessage());
        
        return status;
    }

    @lombok.Data
    @lombok.Builder
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class LicenseStatus {
        private boolean isActivated;
        private boolean isValid;
        private String licenseType;
        private LocalDateTime activatedAt;
        private LocalDateTime expiresAt;
        private long daysRemaining;
        private String message;
        private boolean shouldShowWarning;
        private String warningMessage;
        private String machineId;
        
        // Explicit getters for Jackson serialization with correct property names
        @com.fasterxml.jackson.annotation.JsonGetter("isValid")
        public boolean isValid() {
            return this.isValid;
        }
        
        @com.fasterxml.jackson.annotation.JsonGetter("isActivated")
        public boolean isActivated() {
            return this.isActivated;
        }
        
        @com.fasterxml.jackson.annotation.JsonGetter("shouldShowWarning")
        public boolean isShouldShowWarning() {
            return this.shouldShowWarning;
        }
    }
}

