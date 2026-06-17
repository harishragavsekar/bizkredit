package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.CollateralStatus;
import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.ProductType;
import com.bizkredit.service.CollateralFacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CollateralFacilityController {

    private final CollateralFacilityService service;

    //  Collateral
    // Nested under /api/loan-applications/{appId}/collaterals

    @PostMapping("/api/loan-applications/{appId}/collaterals")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> registerCollateral(
            @PathVariable Long appId,
            @Valid @RequestBody CollateralRecord collateral) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral registered",
                        service.registerCollateral(appId, collateral)));
    }

    @GetMapping("/api/loan-applications/{appId}/collaterals")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CollateralRecord>>> getByApplication(
            @PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.ok("Collaterals fetched",
                service.getCollateralByApplication(appId)));
    }

    @GetMapping("/api/loan-applications/{appId}/collaterals/{id}")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> getCollateral(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral fetched",
                service.getCollateralById(id)));
    }

    @PutMapping("/api/loan-applications/{appId}/collaterals/{id}")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> updateCollateral(
            @PathVariable Long appId,
            @PathVariable Long id,
            @RequestBody CollateralRecord updates) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral updated",
                service.updateCollateral(id, updates)));
    }

    @PatchMapping("/api/loan-applications/{appId}/collaterals/{id}/status")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> updateCollateralStatus(
            @PathVariable Long appId,
            @PathVariable Long id,
            @RequestParam CollateralStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                service.updateCollateralStatus(id, status)));
    }

    // Coverage ratio — sum(RealisableValue) / SanctionedLimit
    @GetMapping("/api/loan-applications/{appId}/collateral-coverage")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getCollateralCoverage(@PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.ok("Coverage ratio fetched",
                service.getCollateralCoverageRatio(appId)));
    }

    @PostMapping("/api/loan-applications/{appId}/collaterals/{id}/revalue")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRevaluation>> revalue(
            @PathVariable Long appId,
            @PathVariable Long id,
            @RequestParam BigDecimal newValue,
            @RequestParam Long revaluedById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral revalued",
                        service.revalueCollateral(id, newValue, revaluedById)));
    }

    @GetMapping("/api/loan-applications/{appId}/collaterals/{id}/revaluations")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CollateralRevaluation>>> getRevaluations(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Revaluations fetched",
                service.getRevaluationHistory(id)));
    }

    //  Facility

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
        return ResponseEntity.ok(ApiResponse.ok("Facility fetched", service.getFacilityById(id)));
    }

    @GetMapping("/api/facilities")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<FacilityAccount>>> getFacilities(
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) FacilityStatus status,
            @RequestParam(required = false) ProductType productType) {
        return ResponseEntity.ok(ApiResponse.ok("Facilities fetched",
                service.getFacilitiesFiltered(businessId, status, productType)));
    }

    @GetMapping("/api/facilities/business/{businessId}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<FacilityAccount>>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(ApiResponse.ok("Facilities fetched",
                service.getFacilitiesByBusiness(businessId)));
    }

    @PutMapping("/api/facilities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacilityAccount>> updateFacility(
            @PathVariable Long id, @RequestBody FacilityAccount updates) {
        return ResponseEntity.ok(ApiResponse.ok("Facility updated", service.updateFacility(id, updates)));
    }

    @PatchMapping("/api/facilities/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<FacilityAccount>> updateFacilityStatus(
            @PathVariable Long id, @RequestParam FacilityStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                service.updateFacilityStatus(id, status)));
    }

    @GetMapping("/api/facilities/expiring")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<FacilityAccount>>> getExpiring(
            @RequestParam(defaultValue = "90") int withinDays) {
        return ResponseEntity.ok(ApiResponse.ok("Expiring facilities fetched",
                service.getExpiringFacilities(withinDays)));
    }

    @PostMapping("/api/facilities/{facilityId}/renew")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> renewFacility(@PathVariable Long facilityId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Renewal application created",
                        service.renewFacility(facilityId)));
    }

    @GetMapping("/api/facilities/{facilityId}/renewal-history")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getRenewalHistory(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Renewal history fetched",
                service.getRenewalHistory(facilityId)));
    }

    // ── Drawdowns — BP2-21 ────────────────────────────────────────

    @PostMapping("/api/facilities/{facilityId}/drawdowns")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','SME_APPLICANT','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> requestDrawdown(
            @PathVariable Long facilityId,
            @Valid @RequestBody Drawdown drawdown) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Drawdown requested",
                        service.requestDrawdown(facilityId, drawdown)));
    }

    @GetMapping("/api/facilities/{facilityId}/drawdowns")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','SME_APPLICANT','ADMIN')")
    public ResponseEntity<ApiResponse<List<Drawdown>>> getDrawdowns(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdowns fetched",
                service.getDrawdownsByFacility(facilityId)));
    }

    @PatchMapping("/api/facilities/{facilityId}/drawdowns/{id}/approve")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> approveDrawdown(
            @PathVariable Long facilityId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown approved", service.approveDrawdown(id)));
    }

    @PatchMapping("/api/facilities/{facilityId}/drawdowns/{id}/disburse")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> disburseDrawdown(
            @PathVariable Long facilityId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown disbursed", service.disburseDrawdown(id)));
    }

    @PatchMapping("/api/facilities/{facilityId}/drawdowns/{id}/repay")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> repayDrawdown(
            @PathVariable Long facilityId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown repaid", service.repayDrawdown(id)));
    }

    @PatchMapping("/api/facilities/{facilityId}/drawdowns/{id}/overdue")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> markOverdue(
            @PathVariable Long facilityId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown marked overdue", service.markOverdue(id)));
    }

    // ── Working Capital Utilisation — BP2-22 ──────────────────────

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

    @GetMapping("/api/facilities/{facilityId}/utilisation/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<WorkingCapitalUtilisation>> getUtilisationById(
            @PathVariable Long facilityId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Utilisation fetched",
                service.getUtilisationById(id)));
    }

    @PutMapping("/api/facilities/{facilityId}/utilisation/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkingCapitalUtilisation>> updateUtilisation(
            @PathVariable Long facilityId,
            @PathVariable Long id,
            @RequestBody WorkingCapitalUtilisation updates) {
        return ResponseEntity.ok(ApiResponse.ok("Utilisation updated",
                service.updateUtilisation(id, updates)));
    }
}
