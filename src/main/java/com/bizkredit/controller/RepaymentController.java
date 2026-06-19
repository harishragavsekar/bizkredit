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
import java.util.Map;

@Tag(name = "Module 4: Facility, Disbursement & Repayment")
@RestController
@RequestMapping("/api/repayments")
@RequiredArgsConstructor
public class RepaymentController {

    private final RepaymentService repaymentService;

    // RECORD REPAYMENT
    @PostMapping
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Object>> recordRepayment(
            @Valid @RequestBody Repayment repayment) {

        Repayment saved = repaymentService.recordRepayment(repayment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Repayment recorded",
                        Map.of(
                                "repaymentId", saved.getRepaymentId(),
                                "status", saved.getStatus()
                        )));
    }

    // GET REPAYMENTS
    @GetMapping
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getRepayments(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long drawdownId) {

        if (drawdownId != null) {
            List<Repayment> list = repaymentService.getByDrawdown(drawdownId);
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched", list));
        }

        if (facilityId != null) {
            List<Repayment> list = repaymentService.getByFacility(facilityId);
            return ResponseEntity.ok(ApiResponse.ok("Repayments fetched", list));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Provide facilityId or drawdownId"));
    }

    //  GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable Long id) {

        Repayment repayment = repaymentService.getById(id);

        return ResponseEntity.ok(ApiResponse.ok(
                "Repayment fetched",
                Map.of(
                        "repaymentId", repayment.getRepaymentId(),
                        "amount", repayment.getAmount(),
                        "status", repayment.getStatus()
                )
        ));
    }

    //  VERIFY REPAYMENT (FINAL FIX HERE)
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Object>> verify(
            @PathVariable Long id,
            @RequestParam Long verifiedById) {

        Repayment repayment = repaymentService.verifyRepayment(id, verifiedById);

        return ResponseEntity.ok(ApiResponse.ok(
                "Repayment verified",
                Map.of(
                        "repaymentId", repayment.getRepaymentId(),
                        "status", repayment.getStatus()
                )
        ));
    }

    //  REVERSE REPAYMENT
    @PutMapping("/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> reverse(@PathVariable Long id) {

        Repayment repayment = repaymentService.reverseRepayment(id);

        return ResponseEntity.ok(ApiResponse.ok(
                "Repayment reversed",
                Map.of(
                        "repaymentId", repayment.getRepaymentId(),
                        "status", repayment.getStatus()
                )
        ));
    }
}