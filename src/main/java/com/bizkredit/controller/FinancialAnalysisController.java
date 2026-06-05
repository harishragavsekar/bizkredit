package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.service.FinancialAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller for Financial Analysis and Credit Underwriting (4.4)
@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialService;

    // ── Financial Statement endpoints ─────────────────────────────

    // Add financial statement - ratios auto-computed
    @PostMapping("/statements")
    public ResponseEntity<ApiResponse<FinancialStatement>> addStatement(
            @RequestParam Long applicationId,
            @RequestBody FinancialStatement statement) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Statement added", financialService.addStatement(applicationId, statement)));
    }

    @GetMapping("/statements/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<FinancialStatement>>> getStatements(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("Statements fetched",
                financialService.getStatementsByApplication(applicationId)));
    }

    @PatchMapping("/statements/{id}/verify")
    public ResponseEntity<ApiResponse<FinancialStatement>> verifyStatement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Statement verified",
                financialService.verifyStatement(id)));
    }

    // ── Credit Proposal endpoints ─────────────────────────────────

    @PostMapping("/proposals")
    public ResponseEntity<ApiResponse<CreditProposal>> createProposal(
            @RequestParam Long applicationId,
            @RequestBody CreditProposal proposal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Proposal created", financialService.createProposal(applicationId, proposal)));
    }

    // Submit proposal for underwriting approval
    @PatchMapping("/proposals/{id}/submit")
    public ResponseEntity<ApiResponse<CreditProposal>> submitProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal submitted",
                financialService.submitProposal(id)));
    }

    @GetMapping("/proposals/{id}")
    public ResponseEntity<ApiResponse<CreditProposal>> getProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal fetched",
                financialService.getProposalById(id)));
    }

    @GetMapping("/proposals/status")
    public ResponseEntity<ApiResponse<List<CreditProposal>>> getByStatus(@RequestParam ProposalStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Proposals fetched",
                financialService.getProposalsByStatus(value)));
    }

    // ── Underwriting Decision endpoints ───────────────────────────

    // Make underwriting decision on a submitted proposal
    @PostMapping("/decisions")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> makeDecision(
            @RequestParam Long proposalId,
            @RequestBody UnderwritingDecision decision) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Decision recorded", financialService.makeDecision(proposalId, decision)));
    }

    @GetMapping("/decisions/proposal/{proposalId}")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> getDecision(@PathVariable Long proposalId) {
        return ResponseEntity.ok(ApiResponse.ok("Decision fetched",
                financialService.getDecisionByProposal(proposalId)));
    }
}
