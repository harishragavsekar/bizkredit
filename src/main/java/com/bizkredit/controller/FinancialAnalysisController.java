package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.CreditProposal;
import com.bizkredit.entity.FinancialStatement;
import com.bizkredit.entity.UnderwritingDecision;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.service.FinancialAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialAnalysisService;

    // POST /api/financial/statements?applicationId=1
    @PostMapping("/statements")
    public ResponseEntity<ApiResponse<FinancialStatement>> addStatement(
            @RequestParam Long applicationId,
            @RequestBody FinancialStatement statement) {
        FinancialStatement saved = financialAnalysisService.addFinancialStatement(applicationId, statement);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Financial statement added", saved));
    }

    // GET /api/financial/statements/application/{applicationId}
    @GetMapping("/statements/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<FinancialStatement>>> getStatements(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("Statements fetched",
                financialAnalysisService.getStatementsByApplication(applicationId)));
    }

    // PATCH /api/financial/statements/{id}/verify
    @PatchMapping("/statements/{id}/verify")
    public ResponseEntity<ApiResponse<FinancialStatement>> verifyStatement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Statement verified",
                financialAnalysisService.verifyStatement(id)));
    }

    // POST /api/financial/proposals?applicationId=1
    @PostMapping("/proposals")
    public ResponseEntity<ApiResponse<CreditProposal>> createProposal(
            @RequestParam Long applicationId,
            @RequestBody CreditProposal proposal) {
        CreditProposal saved = financialAnalysisService.createProposal(applicationId, proposal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Credit proposal created", saved));
    }

    // PATCH /api/financial/proposals/{id}/submit
    @PatchMapping("/proposals/{id}/submit")
    public ResponseEntity<ApiResponse<CreditProposal>> submitProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal submitted",
                financialAnalysisService.submitProposal(id)));
    }

    // GET /api/financial/proposals/{id}
    @GetMapping("/proposals/{id}")
    public ResponseEntity<ApiResponse<CreditProposal>> getProposal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal fetched",
                financialAnalysisService.getProposalById(id)));
    }

    // GET /api/financial/proposals/status?value=SUBMITTED
    @GetMapping("/proposals/status")
    public ResponseEntity<ApiResponse<List<CreditProposal>>> getByStatus(@RequestParam ProposalStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("Proposals fetched",
                financialAnalysisService.getProposalsByStatus(value)));
    }

    // POST /api/financial/decisions?proposalId=1
    @PostMapping("/decisions")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> makeDecision(
            @RequestParam Long proposalId,
            @RequestBody UnderwritingDecision decision) {
        UnderwritingDecision saved = financialAnalysisService.makeDecision(proposalId, decision);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Underwriting decision recorded", saved));
    }

    // GET /api/financial/decisions/proposal/{proposalId}
    @GetMapping("/decisions/proposal/{proposalId}")
    public ResponseEntity<ApiResponse<UnderwritingDecision>> getDecision(@PathVariable Long proposalId) {
        return ResponseEntity.ok(ApiResponse.ok("Decision fetched",
                financialAnalysisService.getDecisionByProposal(proposalId)));
    }
}
