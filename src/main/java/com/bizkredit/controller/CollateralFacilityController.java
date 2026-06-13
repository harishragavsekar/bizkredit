package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.CollateralStatus;
import com.bizkredit.service.CollateralFacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Controller for Collateral (4.5) and Facility Disbursement (4.6)
@RestController
@RequiredArgsConstructor
public class CollateralFacilityController {

    private final CollateralFacilityService service;

    // ── Collateral endpoints ──────────────────────────────────────

    @PostMapping("/api/collateral")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> registerCollateral(
            @RequestParam Long applicationId,
            @Valid @RequestBody CollateralRecord collateral) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral registered",
                        service.registerCollateral(applicationId, collateral)));
    }

    @GetMapping("/api/collateral/{id}")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> getCollateral(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral fetched",
                service.getCollateralById(id)));
    }

    @GetMapping("/api/collateral/application/{applicationId}")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CollateralRecord>>> getByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral fetched",
                service.getCollateralByApplication(applicationId)));
    }

    @PatchMapping("/api/collateral/{id}/status")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> updateStatus(
            @PathVariable Long id, @RequestParam CollateralStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                service.updateCollateralStatus(id, status)));
    }

    @PostMapping("/api/collateral/{id}/revalue")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRevaluation>> revalue(
            @PathVariable Long id,
            @RequestParam BigDecimal newValue,
            @RequestParam Long revaluedById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral revalued",
                        service.revalueCollateral(id, newValue, revaluedById)));
    }

    @GetMapping("/api/collateral/{id}/revaluations")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CollateralRevaluation>>> getRevaluations(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Revaluation history fetched",
                service.getRevaluationHistory(id)));
    }

    // ── Facility endpoints ────────────────────────────────────────

    @PostMapping("/api/facilities")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<FacilityAccount>> createFacility(
            @RequestParam Long applicationId,
            @RequestParam Long businessId,
            @Valid @RequestBody FacilityAccount facility) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Facility created",
                        service.createFacility(applicationId, businessId, facility)));
    }

    @GetMapping("/api/facilities/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FacilityAccount>> getFacility(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility fetched",
                service.getFacilityById(id)));
    }

    @GetMapping("/api/facilities/business/{businessId}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<FacilityAccount>>> getByBusiness(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(ApiResponse.ok("Facilities fetched",
                service.getFacilitiesByBusiness(businessId)));
    }

    // ── Drawdown endpoints ────────────────────────────────────────

    @PostMapping("/api/facilities/{facilityId}/drawdowns")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> requestDrawdown(
            @PathVariable Long facilityId,
            @Valid @RequestBody Drawdown drawdown) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Drawdown requested",
                        service.requestDrawdown(facilityId, drawdown)));
    }

    @PatchMapping("/api/drawdowns/{id}/disburse")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown disbursed",
                service.disburseDrawdown(id)));
    }

    @PatchMapping("/api/drawdowns/{id}/repay")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> repay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown repaid",
                service.repayDrawdown(id)));
    }

    @PatchMapping("/api/drawdowns/{id}/overdue")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> markOverdue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown marked overdue",
                service.markOverdue(id)));
    }

    @GetMapping("/api/facilities/{facilityId}/drawdowns")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<Drawdown>>> getDrawdowns(
            @PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdowns fetched",
                service.getDrawdownsByFacility(facilityId)));
    }

    // ── Working Capital endpoints ─────────────────────────────────

    @PostMapping("/api/facilities/{facilityId}/utilisation")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkingCapitalUtilisation>> recordUtilisation(
            @PathVariable Long facilityId,
            @Valid @RequestBody WorkingCapitalUtilisation utilisation) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Utilisation recorded",
                        service.recordUtilisation(facilityId, utilisation)));
    }

    @GetMapping("/api/facilities/{facilityId}/utilisation")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkingCapitalUtilisation>>> getUtilisation(
            @PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Utilisation fetched",
                service.getUtilisationByFacility(facilityId)));
    }
}