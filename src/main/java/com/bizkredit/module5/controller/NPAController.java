package com.bizkredit.module5.controller;

import com.bizkredit.module1.dto.ApiResponse;
import com.bizkredit.module5.entity.NPARecord;
import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import com.bizkredit.module5.service.NPAClassificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Module 5: Risk Monitoring & Portfolio")
@RestController
@RequestMapping("/api/npa")
@RequiredArgsConstructor
public class NPAController {

    private final NPAClassificationService npaService;

    @PostMapping("/classify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> classify() {
        int count = npaService.runClassification();

        return ResponseEntity.ok(ApiResponse.ok(
                "Classification complete",
                Map.of("newNPAClassified", count)
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<NPARecord>>> getAllNPA(
            @RequestParam(required = false) NPAProvisioningCategory provisioningCategory,
            @RequestParam(required = false) NPARecordStatus status) {

        return ResponseEntity.ok(ApiResponse.ok(
                "NPA records fetched",
                npaService.getAllNPA(provisioningCategory, status)
        ));
    }

    @PostMapping("/{npaId}/upgrade")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<NPARecord>> upgrade(@PathVariable Long npaId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Facility upgraded from NPA",
                npaService.upgradeNPA(npaId)
        ));
    }
}