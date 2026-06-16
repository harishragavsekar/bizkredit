package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.Repayment;
import com.bizkredit.service.RepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<List<Repayment>>> getRepayments(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long drawdownId) {
        if (drawdownId != null) {
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched",
                    repaymentService.getByDrawdown(drawdownId)));
        }
        if (facilityId != null) {
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched",
                    repaymentService.getByFacility(facilityId)));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Provide facilityId or drawdownId"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Repayment fetched", repaymentService.getById(id)));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> verify(
            @PathVariable Long id, @RequestParam Long verifiedById) {
        return ResponseEntity.ok(ApiResponse.ok("Repayment verified",
                repaymentService.verifyRepayment(id, verifiedById)));
    }

    @PutMapping("/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Repayment>> reverse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Repayment reversed",
                repaymentService.reverseRepayment(id)));
    }
}
