package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Reset password for a user by username
     * POST /api/auth/password-reset/reset-by-username
     * Requires: Admin authentication
     * Body: { "username": "admin", "newPassword": "NewPassword@123" }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-by-username")
    public ResponseEntity<Map<String, Object>> resetPasswordByUsername(
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {
        
        log.info("Password reset request for username: {} by user: {}", 
                request.getUsername(), authentication != null ? authentication.getName() : "unknown");
        
        try {
            passwordResetService.resetPasswordByUsername(
                    request.getUsername(), 
                    request.getNewPassword(),
                    authentication != null ? authentication.getName() : "system"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully for user: " + request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting password", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Reset password for a user by user ID
     * POST /api/auth/password-reset/reset-by-id
     * Requires: Admin authentication
     * Body: { "userId": 1, "newPassword": "NewPassword@123" }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-by-id")
    public ResponseEntity<Map<String, Object>> resetPasswordById(
            @Valid @RequestBody ResetPasswordByIdRequest request,
            Authentication authentication) {
        
        log.info("Password reset request for user ID: {} by user: {}", 
                request.getUserId(), authentication != null ? authentication.getName() : "unknown");
        
        try {
            passwordResetService.resetPasswordById(
                    request.getUserId(), 
                    request.getNewPassword(),
                    authentication != null ? authentication.getName() : "system"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully for user ID: " + request.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting password", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Reset admin password (special endpoint for emergency admin password reset)
     * POST /api/auth/password-reset/reset-admin
     * Body: { "newPassword": "NewPassword@123", "adminSecretKey": "EMERGENCY_ADMIN_RESET_KEY" }
     * Note: This endpoint should be protected by a secret key in production
     * Configure secret key in application.yml: app.password.reset.admin-secret-key
     */
    @PostMapping("/reset-admin")
    public ResponseEntity<Map<String, Object>> resetAdminPassword(
            @Valid @RequestBody ResetAdminPasswordRequest request) {
        
        log.warn("Emergency admin password reset requested");
        
        try {
            passwordResetService.resetAdminPassword(request.getNewPassword(), request.getAdminSecretKey());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting admin password", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Data
    public static class ResetPasswordRequest {
        private String username;
        private String newPassword;
    }

    @Data
    public static class ResetPasswordByIdRequest {
        private Long userId;
        private String newPassword;
    }

    @Data
    public static class ResetAdminPasswordRequest {
        private String newPassword;
        private String adminSecretKey;
    }
}
