package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.DecisionStatus;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// Service for Financial Analysis (4.4) module
// Key feature: auto-computes financial ratios when statement is submitted
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialStatementRepository statementRepository;
    private final CreditProposalRepository proposalRepository;
    private final UnderwritingDecisionRepository decisionRepository;
    private final LoanApplicationRepository applicationRepository;

    // ── Financial Statement ───────────────────────────────────────

    // Add financial statement and auto-compute ratios
    public FinancialStatement addStatement(Long applicationId, FinancialStatement statement) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        statement.setApplication(application);
        statement = computeRatios(statement); // auto-calculate ratios

        FinancialStatement saved = statementRepository.save(statement);
        log.info("Financial statement added for application {}, year {}", applicationId, saved.getFinancialYear());
        return saved;
    }

    public List<FinancialStatement> getStatementsByApplication(Long applicationId) {
        return statementRepository.findByApplication_ApplicationId(applicationId);
    }

    // Mark statement as verified after analyst review
    public FinancialStatement verifyStatement(Long statementId) {
        FinancialStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));
        statement.setStatus("Verified");
        log.info("Statement {} verified", statementId);
        return statementRepository.save(statement);
    }

    // ── Credit Proposal ───────────────────────────────────────────

    public CreditProposal createProposal(Long applicationId, CreditProposal proposal) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        proposal.setApplication(application);
        proposal.setStatus(ProposalStatus.DRAFT);
        CreditProposal saved = proposalRepository.save(proposal);
        log.info("Credit proposal created for application {}", applicationId);
        return saved;
    }

    // Submit proposal for underwriting review - only DRAFT can be submitted
    public CreditProposal submitProposal(Long proposalId) {
        CreditProposal proposal = getProposalById(proposalId);
        if (proposal.getStatus() != ProposalStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT proposals can be submitted");
        }
        proposal.setStatus(ProposalStatus.SUBMITTED);
        log.info("Proposal {} submitted for underwriting", proposalId);
        return proposalRepository.save(proposal);
    }

    public CreditProposal getProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));
    }

    public List<CreditProposal> getProposalsByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }

    // ── Underwriting Decision ─────────────────────────────────────

    // Make underwriting decision - proposal must be SUBMITTED first
    public UnderwritingDecision makeDecision(Long proposalId, UnderwritingDecision decision) {
        CreditProposal proposal = getProposalById(proposalId);
        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new BadRequestException("Proposal must be SUBMITTED before a decision can be made");
        }
        decision.setProposal(proposal);

        // Update proposal status based on decision
        if (decision.getStatus() == DecisionStatus.APPROVED ||
            decision.getStatus() == DecisionStatus.CONDITIONAL_APPROVAL) {
            proposal.setStatus(ProposalStatus.APPROVED_BY_MANAGER);
        } else {
            proposal.setStatus(ProposalStatus.DECLINED);
        }
        proposalRepository.save(proposal);

        UnderwritingDecision saved = decisionRepository.save(decision);
        log.info("Underwriting decision {} for proposal {}", decision.getStatus(), proposalId);
        return saved;
    }

    public UnderwritingDecision getDecisionByProposal(Long proposalId) {
        return decisionRepository.findByProposal_ProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found for proposal: " + proposalId));
    }

    // ── Ratio computation ─────────────────────────────────────────

    // Auto-computes 3 key credit ratios from raw financial data
    private FinancialStatement computeRatios(FinancialStatement s) {
        try {
            // Current Ratio = Total Assets / Total Liabilities
            if (s.getTotalAssets() != null && s.getTotalLiabilities() != null
                    && s.getTotalLiabilities().compareTo(BigDecimal.ZERO) != 0) {
                s.setCurrentRatio(s.getTotalAssets()
                        .divide(s.getTotalLiabilities(), 2, RoundingMode.HALF_UP));
            }
            // Debt-Equity Ratio = Total Liabilities / Net Worth
            if (s.getTotalLiabilities() != null && s.getNetWorth() != null
                    && s.getNetWorth().compareTo(BigDecimal.ZERO) != 0) {
                s.setDebtEquityRatio(s.getTotalLiabilities()
                        .divide(s.getNetWorth(), 2, RoundingMode.HALF_UP));
            }
            // DSCR = EBITDA / Total Liabilities (simplified)
            if (s.getEbitda() != null && s.getTotalLiabilities() != null
                    && s.getTotalLiabilities().compareTo(BigDecimal.ZERO) != 0) {
                s.setDscr(s.getEbitda()
                        .divide(s.getTotalLiabilities(), 2, RoundingMode.HALF_UP));
            }
        } catch (Exception e) {
            log.warn("Could not compute some ratios: {}", e.getMessage());
        }
        return s;
    }
}
