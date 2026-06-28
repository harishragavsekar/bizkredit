package com.bizkredit.module1.controller;

import com.bizkredit.common.dto.ApiResponse;
import com.bizkredit.module1.entity.AuditLog;
import com.bizkredit.module1.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Module 1: Auth, Users, Scope & Audit")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getLogsByUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Audit logs fetched",
                auditLogService.getLogsByUserId(userId, page, size)
        ));
    }

    @GetMapping("/{auditId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditLog>> getById(@PathVariable Long auditId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Audit log fetched",
                auditLogService.getById(auditId)
        ));
    }
}