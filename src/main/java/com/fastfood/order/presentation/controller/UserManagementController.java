package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.UserRequest;
import com.fastfood.order.application.dto.UserResponse;
import com.fastfood.order.application.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        log.info("POST /api/admin/users - Creating new user: {}", request.getUsername());
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            UserResponse response = userManagementService.createUser(request, userId);
            log.info("POST /api/admin/users - User created successfully: {}", response.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /api/admin/users - Error creating user", e);
            throw e;
        }
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        log.info("GET /api/admin/users - Fetching all users");
        try {
            List<UserResponse> users = userManagementService.getAllUsers();
            log.info("GET /api/admin/users - Found {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("GET /api/admin/users - Error fetching users", e);
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("GET /api/admin/users/{} - Fetching user", id);
        try {
            UserResponse user = userManagementService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("GET /api/admin/users/{} - Error fetching user", id, e);
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        log.info("PUT /api/admin/users/{} - Updating user", id);
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            UserResponse response = userManagementService.updateUser(id, request, userId);
            log.info("PUT /api/admin/users/{} - User updated successfully", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PUT /api/admin/users/{} - Error updating user", id, e);
            throw e;
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("DELETE /api/admin/users/{} - Deleting user", id);
        try {
            userManagementService.deleteUser(id);
            log.info("DELETE /api/admin/users/{} - User deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("DELETE /api/admin/users/{} - Error deleting user", id, e);
            throw e;
        }
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PATCH /api/admin/users/{}/toggle-status - Toggling user status", id);
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            UserResponse response = userManagementService.toggleUserStatus(id, userId);
            log.info("PATCH /api/admin/users/{}/toggle-status - User status toggled successfully", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PATCH /api/admin/users/{}/toggle-status - Error toggling user status", id, e);
            throw e;
        }
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // For now, return a default user ID. In production, extract from JWT token
            return 1L;
        }
        return 1L;
    }
}

