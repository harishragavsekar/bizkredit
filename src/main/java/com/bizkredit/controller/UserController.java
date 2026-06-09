package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for user management
// Registration moved to AuthController (/api/auth/register)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getUserById(id)));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All users", userService.getAllUsers()));
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.getUsersByRole(role)));
    }

    // Update user status - Active / Locked / Inactive
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.updateStatus(id, value)));
    }

    // Get audit logs for a user
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> auditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", userService.getAuditLogs(id)));
    }
}
