package com.fastfood.order.presentation.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

/**
 * Helper class for common controller operations
 */
@Slf4j
public class ControllerHelper {

    /**
     * Extract user ID from authentication
     * TODO: Extract actual user ID from JWT token claims
     */
    public static Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            log.debug("Extracting user ID for authenticated user: {}", username);
            // TODO: Implement actual user ID extraction from JWT token or user repository
            // For now, return default value
            return 1L;
        }
        log.warn("Authentication is null or not authenticated, using default user ID");
        return 1L;
    }

    /**
     * Get username from authentication
     */
    public static String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}

