package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 1: Auth, Users, Scope & Audit")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RELATIONSHIP_MANAGER','CREDIT_ANALYST','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','SME_APPLICANT')")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getUserById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<List<User>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All users", userService.getAllUsers()));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<List<User>>> getByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.getUsersByRole(role)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.updateStatus(id, value)));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> auditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", userService.getAuditLogs(id)));
    }
}
