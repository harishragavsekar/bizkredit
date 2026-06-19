package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.dto.UserScopeDTOs.UserScopeRequest;
import com.bizkredit.dto.UserScopeDTOs.UserScopeResponse;
import com.bizkredit.entity.User;
import com.bizkredit.service.UserScopeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Module 1: Auth, Users, Scope & Audit")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserScopeController {

    private final UserScopeService scopeService;

    @PatchMapping("/{id}/scope")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserScopeResponse>> assignScope(
            @PathVariable Long id,
            @Valid @RequestBody UserScopeRequest request) {
        User updated = scopeService.assignScope(id, request.branchId(), request.region());
        return ResponseEntity.ok(ApiResponse.ok("Scope assigned", toResponse(updated)));
    }

    @GetMapping("/my-scope")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserScopeResponse>> getMyScope() {
        User user = scopeService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok("Your scope", toResponse(user)));
    }

    private UserScopeResponse toResponse(User u) {
        return new UserScopeResponse(
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getBranchId(),
                u.getRegion(),
                u.getRole().name()
        );
    }
}
