package com.fastfood.order.infrastructure.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastfood.order.application.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enforces a valid license for business APIs (mirrors frontend
 * {@code isLicenseValid === true} / {@code isAdmin && isLicenseValid === true}).
 * <p>
 * Admin role is enforced separately by Spring Security {@code @PreAuthorize} /
 * {@code SecurityConfig}. Combined: admin endpoints require ADMIN + valid license;
 * order endpoints require authentication + valid license.
 * <p>
 * Always allowed without a valid license (so admin can renew / login):
 * license, auth, settings, health, public utilities, file serve/upload.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseValidationInterceptor implements HandlerInterceptor {

    private final LicenseService licenseService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (isExemptFromLicense(path)) {
            return true;
        }

        boolean isValid = licenseService.isLicenseValid();
        if (isValid) {
            response.setHeader("X-License-Status", "VALID");
            return true;
        }

        log.warn("Blocked request due to invalid/expired license: {} {}", request.getMethod(), path);
        response.setHeader("X-License-Status", "INVALID");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", "LICENSE_INVALID");
        body.put("message",
                "License is expired or invalid. Please activate or renew the license in Settings.");
        body.put("path", path);

        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }

    private boolean isExemptFromLicense(String path) {
        if (path == null) {
            return true;
        }
        // Keep settings/license/auth reachable so ADMIN can renew without a valid license
        return path.startsWith("/api/license")
                || path.startsWith("/api/auth")
                || path.startsWith("/api/settings")
                || path.startsWith("/api/public/")
                || path.startsWith("/api/files/")
                || path.startsWith("/api/health")
                || path.startsWith("/api/receipt")
                || path.startsWith("/actuator/health");
    }
}
