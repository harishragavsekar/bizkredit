package com.bizkredit.module3.controller;

import com.bizkredit.module3.entity.FinancialStatement;
import com.bizkredit.module3.entity.CreditProposal;
import com.bizkredit.module3.entity.UnderwritingDecision;
import com.bizkredit.common.dto.ApiResponse;
import com.bizkredit.common.enums.ProposalStatus;
import com.bizkredit.module3.service.FinancialAnalysisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 3: Credit Analysis & Scorecard")
@RestController
@RequiredArgsConstructor
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialService;

    // ── Financial Statements - BP2-16 ────────────────────────────
    // POST /api/loan-applications/{appId}/financial-statements

    @PostMapping("/api/loan-applications/{appId}/financial-statements")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> addStatement(
            @PathVariable Long appId,
            @Valid @RequestBody FinancialStatement statement) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Statement added",
                        financialService.addStatement(appId, statement)));
    }

    @GetMapping("/api/loan-applications/{appId}/financial-statements")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<FinancialStatement>>> getStatements(
            @PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.ok("Statements fetched",
                financialService.getStatementsByApplication(appId)));
    }

    @GetMapping("/api/loan-applications/{appId}/financial-statements/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> getStatement(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Statement fetched",
                financialService.getStatementById(id)));
    }

    @PutMapping("/api/loan-applications/{appId}/financial-statements/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> updateStatement(
            @PathVariable Long appId,
            @PathVariable Long id,
            @RequestBody FinancialStatement updates) {
        return ResponseEntity.ok(ApiResponse.ok("Statement updated",
                financialService.updateStatement(id, updates)));
    }

    @PatchMapping("/api/loan-applications/{appId}/financial-statements/{id}/verify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> verifyStatement(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Statement verified",
                financialService.verifyStatement(id)));
    }

    // ── Credit Proposals - BP2-17 ─────────────────────────────────
    // POST /api/loan-applications/{appId}/credit-proposals

    @PostMapping("/api/loan-applications/{appId}/credit-proposals")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> createProposal(
            @PathVariable Long appId,
            @Valid @RequestBody CreditProposal proposal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Proposal created",
                        financialService.createProposal(appId, proposal)));
    }

    @GetMapping("/api/loan-applications/{appId}/credit-proposals/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> getProposal(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal fetched",
                financialService.getProposalById(id)));
    }

    @PutMapping("/api/loan-applications/{appId}/credit-proposals/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> updateProposal(
            @PathVariable Long appId,
            @PathVariable Long id,
            @RequestBody CreditProposal updates) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal updated",
                financialService.updateProposal(id, updates)));
    }

    @PatchMapping("/api/loan-applications/{appId}/credit-proposals/{id}/submit")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> submitProposal(
            @PathVariable Long appId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal submitted",
                financialService.submitProposal(id)));
    }

    @GetMapping("/api/loan-applications/{appId}/credit-proposals")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CreditProposal>>> getProposalsByStatus(
            @PathVariable Long appId,
            @RequestParam(required = false) ProposalStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Proposals fetched",
                status != null
                        ? financialService.getProposalsByStatus(status)
                        : financialService.getProposalsByApplication(appId)));
    }

    // ── Underwriting Decisions - BP2-18 ──────────────────────────
    // POST /api/credit-proposals/{proposalId}/decisions

    @PostMapping("/api/credit-proposals/{proposalId}/decisions")
    @PreAuthorize("hasAnyRole('UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> makeDecision(
            @PathVariable Long proposalId,
            @Valid @RequestBody UnderwritingDecision decision) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Decision recorded",
                        financialService.makeDecision(proposalId, decision)));
    }

    @GetMapping("/api/credit-proposals/{proposalId}/decisions/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> getDecision(
            @PathVariable Long proposalId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Decision fetched",
                financialService.getDecisionByProposal(proposalId)));
    }

    @GetMapping("/api/credit-proposals/{proposalId}/decisions")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> getDecisionByProposal(
            @PathVariable Long proposalId) {
        return ResponseEntity.ok(ApiResponse.ok("Decision fetched",
                financialService.getDecisionByProposal(proposalId)));
    }
}
