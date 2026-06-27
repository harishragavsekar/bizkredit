package com.bizkredit.module4.controller;

import com.bizkredit.common.dto.ApiResponse;
import com.bizkredit.module4.dto.MakerCheckerDTOs.MakerCheckerRequest;
import com.bizkredit.module4.dto.MakerCheckerDTOs.MakerCheckerActionRequest;

import com.bizkredit.module4.entity.MakerCheckerRecord;
import com.bizkredit.module4.service.MakerCheckerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 4: Facility, Disbursement & Repayment")
@RestController
@RequestMapping("/api/maker-checker")
@RequiredArgsConstructor
public class MakerCheckerController {

    private final MakerCheckerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST', 'RELATIONSHIP_MANAGER', 'COLLATERAL_EVALUATOR')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> submit(
            @Valid @RequestBody MakerCheckerRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Submitted for approval", service.submit(request)));
    }

    @GetMapping("/pending/{role}")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MakerCheckerRecord>>> getPendingForRole(
            @PathVariable String role) {

        return ResponseEntity.ok(ApiResponse.ok("Pending records",
                service.getPendingForRole(role)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) MakerCheckerActionRequest request) {

        String comments = request != null ? request.comments() : null;

        return ResponseEntity.ok(ApiResponse.ok("Record approved",
                service.approve(id, comments)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER', 'RELATIONSHIP_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MakerCheckerRecord>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) MakerCheckerActionRequest request) {

        String comments = request != null ? request.comments() : null;

        return ResponseEntity.ok(ApiResponse.ok("Record rejected",
                service.reject(id, comments)));
    }
}