package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.NPARecord;
import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import com.bizkredit.service.NPAClassificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Module 5: Risk Monitoring & Portfolio", description = "NPA classification and provisioning")
@RestController
@RequestMapping("/api/npa")
@RequiredArgsConstructor
public class NPAController {

    private final NPAClassificationService npaService;

    // POST /api/npa/classify — run classification engine
    @PostMapping("/classify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> classify() {
        int count = npaService.runClassification();
        return ResponseEntity.ok(ApiResponse.ok("Classification complete",
                Map.of("newNPAClassified", count)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<NPARecord>>> getAllNPA(
            @RequestParam(required = false) NPAProvisioningCategory provisioningCategory,
            @RequestParam(required = false) NPARecordStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("NPA records fetched",
                npaService.getAllNPA(provisioningCategory, status)));
    }

    @GetMapping("/{facilityId}/history")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<NPARecord>>> getHistory(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("NPA history fetched",
                npaService.getHistoryByFacility(facilityId)));
    }

    @PutMapping("/{npaId}/upgrade")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<NPARecord>> upgrade(@PathVariable Long npaId) {
        return ResponseEntity.ok(ApiResponse.ok("Facility upgraded from NPA",
                npaService.upgradeNPA(npaId)));
    }
}
