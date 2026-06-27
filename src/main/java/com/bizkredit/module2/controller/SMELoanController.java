package com.bizkredit.module2.controller;

import com.bizkredit.module2.entity.SMEBusiness;
import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.module2.entity.Promoter;
import com.bizkredit.module2.entity.ApplicationDocument;
import com.bizkredit.module2.entity.GroupCompany;
import com.bizkredit.common.dto.ApiResponse;
import com.bizkredit.common.enums.ApplicationStatus;
import com.bizkredit.common.enums.ProductType;
import com.bizkredit.common.enums.VerificationStatus;
import com.bizkredit.module2.service.SMELoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 2: SME Onboarding & Loan Origination")
@RestController
@RequiredArgsConstructor
public class SMELoanController {

    private final SMELoanService smeService;

    //  SME Business

    @PostMapping("/api/sme-businesses")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> registerBusiness(@Valid @RequestBody SMEBusiness business) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Business registered", smeService.registerBusiness(business)));
    }

    @GetMapping("/api/sme-businesses/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> getBusiness(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Business fetched", smeService.getBusinessById(id)));
    }

    @GetMapping("/api/sme-businesses")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SMEBusiness>>> getAllBusinesses(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok("Businesses fetched",
                smeService.getBusinessesFiltered(entityType, industry, status)));
    }

    @PutMapping("/api/sme-businesses/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateBusiness(
            @PathVariable Long id, @RequestBody SMEBusiness updates) {
        return ResponseEntity.ok(ApiResponse.ok("Business updated", smeService.updateBusiness(id, updates)));
    }

    @PatchMapping("/api/sme-businesses/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateBusinessStatus(
            @PathVariable Long id, @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", smeService.updateBusinessStatus(id, value)));
    }

    // BP2-13 - KYC status update
    @PatchMapping("/api/sme-businesses/{id}/kyc-status")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateKyc(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("KYC updated", smeService.updateKycStatus(id, status)));
    }

    // BP2-13 - Promoters linked to business
    @PostMapping("/api/sme-businesses/{id}/promoters")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Promoter>> addPromoter(
            @PathVariable Long id, @Valid @RequestBody Promoter promoter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Promoter added", smeService.addPromoter(id, promoter)));
    }

    @GetMapping("/api/sme-businesses/{id}/promoters")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<Promoter>>> getPromoters(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Promoters fetched", smeService.getPromotersByBusiness(id)));
    }

    @PutMapping("/api/sme-businesses/{businessId}/promoters/{promoterId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Promoter>> updatePromoter(
            @PathVariable Long businessId,
            @PathVariable Long promoterId,
            @RequestBody Promoter updates) {
        return ResponseEntity.ok(ApiResponse.ok("Promoter updated",
                smeService.updatePromoter(promoterId, updates)));
    }

    @DeleteMapping("/api/sme-businesses/{businessId}/promoters/{promoterId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromoter(
            @PathVariable Long businessId,
            @PathVariable Long promoterId) {
        smeService.deletePromoter(promoterId);
        return ResponseEntity.ok(ApiResponse.ok("Promoter removed", null));
    }

    // Group companies
    @PostMapping("/api/sme-businesses/{id}/group-companies")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<GroupCompany>> linkGroup(
            @PathVariable Long id,
            @RequestParam Long subsidiaryId,
            @RequestParam String relationship) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group linked", smeService.linkGroupCompany(id, subsidiaryId, relationship)));
    }

    @GetMapping("/api/sme-businesses/{id}/group-companies")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupCompany>>> getGroupCompanies(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Group companies fetched",
                smeService.getGroupCompaniesByBusiness(id)));
    }

    // Loan Application

    @PostMapping("/api/loan-applications")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> createApplication(
            @RequestParam Long businessId,
            @Valid @RequestBody LoanApplication application) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application created", smeService.createApplication(businessId, application)));
    }

    @PutMapping("/api/loan-applications/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> updateApplication(
            @PathVariable Long id, @RequestBody LoanApplication updates) {
        return ResponseEntity.ok(ApiResponse.ok("Application updated", smeService.updateApplication(id, updates)));
    }

    @PatchMapping("/api/loan-applications/{id}/submit")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> submitApplication(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Application submitted", smeService.submitDraftApplication(id)));
    }

    @GetMapping("/api/loan-applications/{id}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Application fetched", smeService.getApplicationById(id)));
    }

    @GetMapping("/api/loan-applications")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getApplications(
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) ProductType productType) {
        return ResponseEntity.ok(ApiResponse.ok("Applications fetched",
                smeService.getApplicationsFiltered(businessId, status, productType)));
    }

    @PatchMapping("/api/loan-applications/{id}/assign")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> assignAnalyst(
            @PathVariable Long id, @RequestParam Long analystId) {
        return ResponseEntity.ok(ApiResponse.ok("Analyst assigned", smeService.assignAnalyst(id, analystId)));
    }

    @PatchMapping("/api/loan-applications/{id}/status")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplication>> updateStatus(
            @PathVariable Long id, @RequestParam ApplicationStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", smeService.updateStatus(id, value)));
    }

    // Documents

    @PostMapping("/api/loan-applications/{appId}/documents")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> uploadDocument(
            @PathVariable Long appId, @Valid @RequestBody ApplicationDocument document) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document uploaded", smeService.uploadDocument(appId, document)));
    }

    @GetMapping("/api/loan-applications/{appId}/documents")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ApplicationDocument>>> getDocuments(@PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.ok("Documents fetched", smeService.getDocumentsByApplication(appId)));
    }

    @GetMapping("/api/loan-applications/{appId}/documents/{docId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> getDocument(
            @PathVariable Long appId, @PathVariable Long docId) {
        return ResponseEntity.ok(ApiResponse.ok("Document fetched", smeService.getDocumentById(docId)));
    }

    @PatchMapping("/api/loan-applications/{appId}/documents/{docId}/verify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> verifyDocument(
            @PathVariable Long appId, @PathVariable Long docId,
            @RequestParam VerificationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Document verified", smeService.verifyDocument(docId, status)));
    }

    @PatchMapping("/api/loan-applications/{appId}/documents/{docId}/flag-deficient")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> flagDeficient(
            @PathVariable Long appId, @PathVariable Long docId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.ok("Document flagged as deficient",
                smeService.flagDeficient(docId, reason)));
    }

    @PatchMapping("/api/loan-applications/{appId}/documents/{docId}/reject")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDocument>> rejectDocument(
            @PathVariable Long appId, @PathVariable Long docId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.ok("Document rejected",
                smeService.rejectDocument(docId, reason)));
    }

    @DeleteMapping("/api/loan-applications/{appId}/documents/{docId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long appId, @PathVariable Long docId) {
        smeService.deleteDocument(docId);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted for re-upload", null));
    }
}
