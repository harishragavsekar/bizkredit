package com.bizkredit.service;

import com.bizkredit.entity.CreditProposal;
import com.bizkredit.entity.FinancialStatement;
import com.bizkredit.entity.LoanApplication;
import com.bizkredit.entity.UnderwritingDecision;
import com.bizkredit.enums.DecisionStatus;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.CreditProposalRepository;
import com.bizkredit.repository.FinancialStatementRepository;
import com.bizkredit.repository.LoanApplicationRepository;
import com.bizkredit.repository.UnderwritingDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialStatementRepository financialStatementRepository;
    private final CreditProposalRepository creditProposalRepository;
    private final UnderwritingDecisionRepository underwritingDecisionRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    // ── Financial Statement ───────────────────────────────────────

    public FinancialStatement addFinancialStatement(Long applicationId, FinancialStatement statement) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        statement.setApplication(application);

        // Auto-compute ratios if raw data is provided
        statement = computeRatios(statement);

        FinancialStatement saved = financialStatementRepository.save(statement);
        log.info("Financial statement added for application {}, year {}", applicationId, saved.getFinancialYear());
        return saved;
    }

    public List<FinancialStatement> getStatementsByApplication(Long applicationId) {
        return financialStatementRepository.findByApplication_ApplicationId(applicationId);
    }

    public FinancialStatement verifyStatement(Long statementId) {
        FinancialStatement statement = financialStatementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));
        statement.setStatus("Verified");
        log.info("Financial statement {} verified", statementId);
        return financialStatementRepository.save(statement);
    }

    // ── Credit Proposal ───────────────────────────────────────────

    public CreditProposal createProposal(Long applicationId, CreditProposal proposal) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        proposal.setApplication(application);
        proposal.setStatus(ProposalStatus.DRAFT);

        CreditProposal saved = creditProposalRepository.save(proposal);
        log.info("Credit proposal created for application {}", applicationId);
        return saved;
    }

    public CreditProposal submitProposal(Long proposalId) {
        CreditProposal proposal = getProposalById(proposalId);
        if (proposal.getStatus() != ProposalStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT proposals can be submitted");
        }
        proposal.setStatus(ProposalStatus.SUBMITTED);
        log.info("Credit proposal {} submitted for underwriting", proposalId);
        return creditProposalRepository.save(proposal);
    }

    public CreditProposal getProposalById(Long proposalId) {
        return creditProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));
    }

    public List<CreditProposal> getProposalsByStatus(ProposalStatus status) {
        return creditProposalRepository.findByStatus(status);
    }

    // ── Underwriting Decision ─────────────────────────────────────

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

        creditProposalRepository.save(proposal);
        UnderwritingDecision saved = underwritingDecisionRepository.save(decision);
        log.info("Underwriting decision {} made for proposal {}", decision.getStatus(), proposalId);
        return saved;
    }

    public UnderwritingDecision getDecisionByProposal(Long proposalId) {
        return underwritingDecisionRepository.findByProposal_ProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found for proposal: " + proposalId));
    }

    // ── Ratio computation helper ──────────────────────────────────

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
