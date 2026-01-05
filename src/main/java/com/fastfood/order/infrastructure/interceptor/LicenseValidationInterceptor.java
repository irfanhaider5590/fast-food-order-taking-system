package com.fastfood.order.infrastructure.interceptor;

import com.fastfood.order.application.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseValidationInterceptor implements HandlerInterceptor {

    private final LicenseService licenseService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip license check for license endpoints, login, settings, and public endpoints
        String path = request.getRequestURI();
        if (path.contains("/api/license") || 
            path.contains("/api/auth/login") ||
            path.contains("/api/settings") ||
            path.contains("/api/public/") ||
            path.contains("/actuator/health") ||
            path.contains("/api/health")) {
            return true;
        }

        // Check license validity - but don't block, just add header
        boolean isValid = licenseService.isLicenseValid();
        if (!isValid) {
            log.warn("License validation failed for request: {}", path);
            // Add header to indicate license status instead of blocking
            response.setHeader("X-License-Status", "INVALID");
        } else {
            response.setHeader("X-License-Status", "VALID");
        }

        return true;
    }
}

