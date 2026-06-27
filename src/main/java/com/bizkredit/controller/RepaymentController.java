package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.Repayment;
import com.bizkredit.service.RepaymentService;
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
@RequestMapping("/api/repayments")
@RequiredArgsConstructor
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> recordRepayment(
            @Valid @RequestBody Repayment repayment) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Repayment recorded",
                        repaymentService.recordRepayment(repayment)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getRepayments(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long drawdownId) {

        if (drawdownId != null) {
            List<Repayment> repayments = repaymentService.getByDrawdown(drawdownId);
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched", repayments));
        }

        if (facilityId != null) {
            List<Repayment> repayments = repaymentService.getByFacility(facilityId);
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched", repayments));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Provide facilityId or drawdownId"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> getById(@PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok("Repayment fetched",
                repaymentService.getById(id)));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> verify(
            @PathVariable Long id,
            @RequestParam Long verifiedById) {

        return ResponseEntity.ok(ApiResponse.ok("Repayment verified",
                repaymentService.verifyRepayment(id, verifiedById)));
    }
}