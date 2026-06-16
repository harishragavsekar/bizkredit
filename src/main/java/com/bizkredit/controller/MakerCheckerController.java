package com.bizkredit.controller;

import com.bizkredit.dto.MakerCheckerDTOs.MakerCheckerActionRequest;
import com.bizkredit.dto.MakerCheckerDTOs.MakerCheckerRequest;
import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.MakerCheckerRecord;
import com.bizkredit.service.MakerCheckerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maker-checker")
@RequiredArgsConstructor
public class MakerCheckerController {

    private final MakerCheckerService service;

    // ── Maker submits an action for approval ──────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST', 'RELATIONSHIP_MANAGER', 'COLLATERAL_EVALUATOR')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> submit(
            @Valid @RequestBody MakerCheckerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Submitted for approval", service.submit(request)));
    }

    // ── Checker sees pending items for their role ─────────────────
    @GetMapping("/pending/{role}")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MakerCheckerRecord>>> getPendingForRole(
            @PathVariable String role) {
        return ResponseEntity.ok(ApiResponse.ok("Pending records", service.getPendingForRole(role)));
    }

    // ── Maker views their own submissions ─────────────────────────
    @GetMapping("/my-submissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MakerCheckerRecord>>> getMySubmissions() {
        return ResponseEntity.ok(ApiResponse.ok("Your submissions", service.getMySubmissions()));
    }

    // ── Admin views all records ───────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MakerCheckerRecord>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All maker-checker records", service.getAll()));
    }

    // ── Get single record ─────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Record found", service.getById(id)));
    }

    // ── Checker approves ──────────────────────────────────────────
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) MakerCheckerActionRequest request) {
        String comments = request != null ? request.comments() : null;
        return ResponseEntity.ok(ApiResponse.ok("Record approved", service.approve(id, comments)));
    }

    // ── Checker rejects ───────────────────────────────────────────
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) MakerCheckerActionRequest request) {
        String comments = request != null ? request.comments() : null;
        return ResponseEntity.ok(ApiResponse.ok("Record rejected", service.reject(id, comments)));
    }

    // ── Maker cancels their own submission ────────────────────────
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Record cancelled", service.cancel(id)));
    }

    // ── View history by entity ────────────────────────────────────
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MakerCheckerRecord>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.ok("Entity records",
                service.getByEntity(entityType, entityId)));
    }
}
