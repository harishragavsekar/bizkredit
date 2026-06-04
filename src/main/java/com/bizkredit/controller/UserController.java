package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody User user) {
        User created = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", created));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getUserById(id)));
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All users", userService.getAllUsers()));
    }

    // PATCH /api/users/{id}/status?value=Locked
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.updateStatus(id, value)));
    }

    // GET /api/users/{id}/audit-logs
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> auditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", userService.getAuditLogs(id)));
    }
}
