package com.fastfood.order.presentation.controller;

import com.fastfood.order.application.dto.UserRequest;
import com.fastfood.order.application.dto.UserResponse;
import com.fastfood.order.application.service.UserManagementService;
import com.fastfood.order.presentation.helper.ControllerHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user management operations (Admin only)
 */
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
        log.info("POST /api/admin/users - Creating user: {}", request.getUsername());
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        UserResponse response = userManagementService.createUser(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        log.debug("GET /api/admin/users - Fetching all users");
        List<UserResponse> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            Authentication authentication) {
        log.debug("GET /api/admin/users/{} - Fetching user", id);
        UserResponse user = userManagementService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        log.info("PUT /api/admin/users/{} - Updating user", id);
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        UserResponse response = userManagementService.updateUser(id, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("DELETE /api/admin/users/{} - Deleting user", id);
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PATCH /api/admin/users/{}/toggle-status - Toggling status", id);
        Long userId = ControllerHelper.getUserIdFromAuthentication(authentication);
        UserResponse response = userManagementService.toggleUserStatus(id, userId);
        return ResponseEntity.ok(response);
    }
}

