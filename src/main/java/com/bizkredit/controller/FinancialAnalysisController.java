package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.service.FinancialAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller for Financial Analysis and Credit Underwriting (4.4)
@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialService;

    // ── Financial Statement endpoints ─────────────────────────────

    @PostMapping("/statements")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> addStatement(
            @RequestParam Long applicationId,
            @Valid @RequestBody FinancialStatement statement) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Statement added", financialService.addStatement(applicationId, statement)));
    }

    @GetMapping("/statements/application/{applicationId}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<FinancialStatement>>> getStatements(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("Statements fetched",
                financialService.getStatementsByApplication(applicationId)));
    }

    @PatchMapping("/statements/{id}/verify")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialStatement>> verifyStatement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Statement verified",
                financialService.verifyStatement(id)));
    }

    // ── Credit Proposal endpoints ─────────────────────────────────

    @PostMapping("/proposals")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> createProposal(
            @RequestParam Long applicationId,
            @Valid @RequestBody CreditProposal proposal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Proposal created", financialService.createProposal(applicationId, proposal)));
    }

    @PatchMapping("/proposals/{id}/submit")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> submitProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal submitted",
                financialService.submitProposal(id)));
    }

    @GetMapping("/proposals/{id}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<CreditProposal>> getProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal fetched",
                financialService.getProposalById(id)));
    }

    @GetMapping("/proposals/status")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CreditProposal>>> getByStatus(@RequestParam ProposalStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Proposals fetched",
                financialService.getProposalsByStatus(value)));
    }

    // ── Underwriting Decision endpoints ───────────────────────────

    @PostMapping("/decisions")
    @PreAuthorize("hasRole('UNDERWRITING_MANAGER')")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> makeDecision(
            @RequestParam Long proposalId,
            @Valid @RequestBody UnderwritingDecision decision) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Decision recorded", financialService.makeDecision(proposalId, decision)));
    }

    @GetMapping("/decisions/proposal/{proposalId}")
    @PreAuthorize("hasAnyRole('CREDIT_ANALYST','UNDERWRITING_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> getDecision(@PathVariable Long proposalId) {
        return ResponseEntity.ok(ApiResponse.ok("Decision fetched",
                financialService.getDecisionByProposal(proposalId)));
    }
}