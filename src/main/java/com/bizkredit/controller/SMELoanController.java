package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.service.SMELoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SMELoanController {

    private final SMELoanService smeService;

    // ── SME Business endpoints ────────────────────────────────────

    @PostMapping("/api/businesses")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> registerBusiness(@Valid @RequestBody SMEBusiness business) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Business registered", smeService.registerBusiness(business)));
    }

    @GetMapping("/api/businesses/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> getBusiness(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Business fetched", smeService.getBusinessById(id)));
    }

    @GetMapping("/api/businesses")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SMEBusiness>>> getAllBusinesses() {
        return ResponseEntity.ok(ApiResponse.ok("All businesses", smeService.getAllBusinesses()));
    }

    @PatchMapping("/api/businesses/{id}/kyc")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateKyc(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("KYC updated", smeService.updateKycStatus(id, status)));
    }

    @PostMapping("/api/businesses/{id}/promoters")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Promoter>> addPromoter(
            @PathVariable Long id, @Valid @RequestBody Promoter promoter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Promoter added", smeService.addPromoter(id, promoter)));
    }

    @GetMapping("/api/businesses/{id}/promoters")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<Promoter>>> getPromoters(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Promoters fetched", smeService.getPromotersByBusiness(id)));
    }

    @PostMapping("/api/businesses/{parentId}/group-links")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<GroupCompany>> linkGroup(
            @PathVariable Long parentId,
            @RequestParam Long subsidiaryId,
            @RequestParam String relationship) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group linked", smeService.linkGroupCompany(parentId, subsidiaryId, relationship)));
    }

    // ── Loan Application endpoints ────────────────────────────────

    @PostMapping("/api/applications")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> submitApplication(
            @RequestParam Long businessId,
            @Valid @RequestBody LoanApplication application) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application submitted", smeService.submitApplication(businessId, application)));
    }

    @GetMapping("/api/applications/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Application fetched", smeService.getApplicationById(id)));
    }

    @GetMapping("/api/applications/business/{businessId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(ApiResponse.ok("Applications fetched", smeService.getApplicationsByBusiness(businessId)));
    }

    @PatchMapping("/api/applications/{id}/assign")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> assignAnalyst(
            @PathVariable Long id, @RequestParam Long analystId) {
        return ResponseEntity.ok(ApiResponse.ok("Analyst assigned", smeService.assignAnalyst(id, analystId)));
    }

    @PatchMapping("/api/applications/{id}/status")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> updateStatus(
            @PathVariable Long id, @RequestParam ApplicationStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", smeService.updateStatus(id, value)));
    }

    @PostMapping("/api/applications/{id}/documents")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> uploadDocument(
            @PathVariable Long id, @Valid @RequestBody ApplicationDocument document) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document uploaded", smeService.uploadDocument(id, document)));
    }

    @GetMapping("/api/applications/{id}/documents")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ApplicationDocument>>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Documents fetched", smeService.getDocumentsByApplication(id)));
    }

    @PatchMapping("/api/applications/documents/{docId}/verify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> verifyDocument(
            @PathVariable Long docId, @RequestParam VerificationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Document verified", smeService.verifyDocument(docId, status)));
    }
}