package com.bizkredit.module4.controller;

import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.module4.entity.CollateralRecord;
import com.bizkredit.module4.entity.CollateralRevaluation;
import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.module4.entity.WorkingCapitalUtilisation;
import com.bizkredit.common.dto.ApiResponse;
import com.bizkredit.module4.service.CollateralFacilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Module 4: Facility, Disbursement & Repayment")
@RestController
@RequiredArgsConstructor
public class CollateralFacilityController {

    private final CollateralFacilityService service;

    // Collateral

    @PostMapping("/api/loan-applications/{appId}/collaterals")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> registerCollateral(
            @PathVariable Long appId,
            @Valid @RequestBody CollateralRecord collateral) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collateral registered",
                        service.registerCollateral(appId, collateral)));
    }

    @GetMapping("/api/loan-applications/{appId}/collaterals/{id}")
    @PreAuthorize("hasAnyRole('COLLATERAL_EVALUATOR','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CollateralRecord>> getCollateral(
            @PathVariable Long appId,
            @PathVariable Long id) {

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

    // Facility

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
    public ResponseEntity<ApiResponse<FacilityAccount>> getFacility(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok("Facility fetched",
                service.getFacilityById(id)));
    }

    @PutMapping("/api/facilities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacilityAccount>> updateFacility(
            @PathVariable Long id,
            @RequestBody FacilityAccount updates) {

        return ResponseEntity.ok(ApiResponse.ok("Facility updated",
                service.updateFacility(id, updates)));
    }

    // Drawdowns

    @PostMapping("/api/facilities/{facilityId}/drawdowns")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','SME_APPLICANT','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> requestDrawdown(
            @PathVariable Long facilityId,
            @Valid @RequestBody Drawdown drawdown) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Drawdown requested",
                        service.requestDrawdown(facilityId, drawdown)));
    }

    @GetMapping("/api/facilities/{facilityId}/drawdowns/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','SME_APPLICANT','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> getDrawdownById(
            @PathVariable Long facilityId,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok("Drawdown fetched",
                service.getDrawdownById(id)));
    }

    @PatchMapping("/api/facilities/{facilityId}/drawdowns/{id}/disburse")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Drawdown>> disburseDrawdown(
            @PathVariable Long facilityId,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok("Drawdown disbursed",
                service.disburseDrawdown(id)));
    }

    // Working Capital Utilisation

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