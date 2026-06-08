package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.CollateralStatus;
import com.bizkredit.service.CollateralFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Controller for Collateral Management (4.5) and Facility Disbursement (4.6)
@RestController
@RequiredArgsConstructor
public class CollateralFacilityController {

    private final CollateralFacilityService collateralFacilityService;

    // ── Collateral endpoints ──────────────────────────────────────

    // Register collateral - realisable value auto-computed
    @PostMapping("/api/collateral")
    public ResponseEntity<ApiResponse<CollateralRecord>> registerCollateral(
            @RequestParam Long applicationId,
            @RequestBody CollateralRecord collateral) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral registered",
                        collateralFacilityService.registerCollateral(applicationId, collateral)));
    }

    @GetMapping("/api/collateral/{id}")
    public ResponseEntity<ApiResponse<CollateralRecord>> getCollateral(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral fetched",
                collateralFacilityService.getCollateralById(id)));
    }

    @GetMapping("/api/collateral/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<CollateralRecord>>> getByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("Collateral fetched",
                collateralFacilityService.getCollateralByApplication(applicationId)));
    }

    @PatchMapping("/api/collateral/{id}/status")
    public ResponseEntity<ApiResponse<CollateralRecord>> updateStatus(
            @PathVariable Long id, @RequestParam CollateralStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                collateralFacilityService.updateCollateralStatus(id, status)));
    }

    // Revalue collateral - tracks % change from previous value
    @PostMapping("/api/collateral/{id}/revalue")
    public ResponseEntity<ApiResponse<CollateralRevaluation>> revalue(
            @PathVariable Long id,
            @RequestParam BigDecimal newValue,
            @RequestParam Long revaluedById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral revalued",
                        collateralFacilityService.revalueCollateral(id, newValue, revaluedById)));
    }

    @GetMapping("/api/collateral/{id}/revaluations")
    public ResponseEntity<ApiResponse<List<CollateralRevaluation>>> getRevaluations(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Revaluation history fetched",
                collateralFacilityService.getRevaluationHistory(id)));
    }

    // ── Facility endpoints ────────────────────────────────────────

    // Create facility account after sanction
    @PostMapping("/api/facilities")
    public ResponseEntity<ApiResponse<FacilityAccount>> createFacility(
            @RequestParam Long applicationId,
            @RequestParam Long businessId,
            @RequestBody FacilityAccount facility) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Facility created",
                        collateralFacilityService.createFacility(applicationId, businessId, facility)));
    }

    @GetMapping("/api/facilities/{id}")
    public ResponseEntity<ApiResponse<FacilityAccount>> getFacility(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility fetched",
                collateralFacilityService.getFacilityById(id)));
    }

    @GetMapping("/api/facilities/business/{businessId}")
    public ResponseEntity<ApiResponse<List<FacilityAccount>>> getFacilitiesByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(ApiResponse.ok("Facilities fetched",
                collateralFacilityService.getFacilitiesByBusiness(businessId)));
    }

    // Request a drawdown against facility limit
    @PostMapping("/api/facilities/{facilityId}/drawdowns")
    public ResponseEntity<ApiResponse<Drawdown>> requestDrawdown(
            @PathVariable Long facilityId,
            @RequestBody Drawdown drawdown) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Drawdown requested",
                        collateralFacilityService.requestDrawdown(facilityId, drawdown)));
    }

    // Disburse a drawdown - updates facility balance
    @PatchMapping("/api/drawdowns/{id}/disburse")
    public ResponseEntity<ApiResponse<Drawdown>> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdown disbursed",
                collateralFacilityService.disburseDrawdown(id)));
    }

    @GetMapping("/api/facilities/{facilityId}/drawdowns")
    public ResponseEntity<ApiResponse<List<Drawdown>>> getDrawdowns(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Drawdowns fetched",
                collateralFacilityService.getDrawdownsByFacility(facilityId)));
    }

    // Record working capital utilisation for the period
    @PostMapping("/api/facilities/{facilityId}/utilisation")
    public ResponseEntity<ApiResponse<WorkingCapitalUtilisation>> recordUtilisation(
            @PathVariable Long facilityId,
            @RequestBody WorkingCapitalUtilisation utilisation) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Utilisation recorded",
                        collateralFacilityService.recordUtilisation(facilityId, utilisation)));
    }

    @GetMapping("/api/facilities/{facilityId}/utilisation")
    public ResponseEntity<ApiResponse<List<WorkingCapitalUtilisation>>> getUtilisation(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Utilisation fetched",
                collateralFacilityService.getUtilisationByFacility(facilityId)));
    }
}
