package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.ApplicationDocument;
import com.bizkredit.entity.LoanApplication;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    // POST /api/applications?businessId=1
    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplication>> submit(
            @RequestParam Long businessId,
            @Valid @RequestBody LoanApplication application) {
        LoanApplication saved = loanApplicationService.submitApplication(businessId, application);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application submitted", saved));
    }

    // GET /api/applications/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Application fetched", loanApplicationService.getApplicationById(id)));
    }

    // GET /api/applications/business/{businessId}
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(ApiResponse.ok("Applications fetched", loanApplicationService.getApplicationsByBusiness(businessId)));
    }

    // GET /api/applications/status?value=SUBMITTED
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getByStatus(@RequestParam ApplicationStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Applications fetched", loanApplicationService.getApplicationsByStatus(value)));
    }

    // PATCH /api/applications/{id}/status?value=IN_REVIEW
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LoanApplication>> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", loanApplicationService.updateStatus(id, value)));
    }

    // PATCH /api/applications/{id}/assign?analystId=2
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LoanApplication>> assignAnalyst(
            @PathVariable Long id,
            @RequestParam Long analystId) {
        return ResponseEntity.ok(ApiResponse.ok("Analyst assigned", loanApplicationService.assignAnalyst(id, analystId)));
    }

    // POST /api/applications/{id}/documents
    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<ApplicationDocument>> uploadDocument(
            @PathVariable Long id,
            @RequestBody ApplicationDocument document) {
        ApplicationDocument saved = loanApplicationService.uploadDocument(id, document);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document uploaded", saved));
    }

    // GET /api/applications/{id}/documents
    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<ApplicationDocument>>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Documents fetched", loanApplicationService.getDocumentsByApplication(id)));
    }

    // PATCH /api/applications/documents/{docId}/verify?status=VERIFIED
    @PatchMapping("/documents/{docId}/verify")
    public ResponseEntity<ApiResponse<ApplicationDocument>> verifyDocument(
            @PathVariable Long docId,
            @RequestParam VerificationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Verification status updated", loanApplicationService.updateVerificationStatus(docId, status)));
    }
}
