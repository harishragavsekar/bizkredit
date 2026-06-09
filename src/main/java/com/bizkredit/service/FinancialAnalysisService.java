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
// Uses Java 21 features: var, switch expressions, pattern matching
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialStatementRepository statementRepository;
    private final CreditProposalRepository proposalRepository;
    private final UnderwritingDecisionRepository decisionRepository;
    private final LoanApplicationRepository applicationRepository;

    public FinancialStatement addStatement(Long applicationId, FinancialStatement statement) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        statement.setApplication(application);
        statement = computeRatios(statement);

        var saved = statementRepository.save(statement);
        log.info("Financial statement added for application {}, year {}", applicationId, saved.getFinancialYear());
        return saved;
    }

    public List<FinancialStatement> getStatementsByApplication(Long applicationId) {
        return statementRepository.findByApplication_ApplicationId(applicationId);
    }

    public FinancialStatement verifyStatement(Long statementId) {
        var statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));
        statement.setStatus("Verified");
        log.info("Statement {} verified", statementId);
        return statementRepository.save(statement);
    }

    public CreditProposal createProposal(Long applicationId, CreditProposal proposal) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        proposal.setApplication(application);
        proposal.setStatus(ProposalStatus.DRAFT);
        var saved = proposalRepository.save(proposal);
        log.info("Credit proposal created for application {}", applicationId);
        return saved;
    }

    public CreditProposal submitProposal(Long proposalId) {
        var proposal = getProposalById(proposalId);

        // Java 14+ switch expression for status validation
        var message = switch (proposal.getStatus()) {
            case DRAFT -> null; // can proceed
            case SUBMITTED -> "Proposal already submitted";
            case APPROVED_BY_MANAGER -> "Proposal already approved";
            case DECLINED -> "Proposal was declined";
            case SANCTIONED -> "Proposal already sanctioned";
        };

        if (message != null) throw new BadRequestException(message);

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

    public UnderwritingDecision makeDecision(Long proposalId, UnderwritingDecision decision) {
        var proposal = getProposalById(proposalId);

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new BadRequestException("Proposal must be SUBMITTED before a decision can be made");
        }

        decision.setProposal(proposal);

        // Switch expression to update proposal based on decision
        var newProposalStatus = switch (decision.getStatus()) {
            case APPROVED, CONDITIONAL_APPROVAL -> ProposalStatus.APPROVED_BY_MANAGER;
            case DECLINED -> ProposalStatus.DECLINED;
        };

        proposal.setStatus(newProposalStatus);
        proposalRepository.save(proposal);

        var saved = decisionRepository.save(decision);
        log.info("Underwriting decision {} for proposal {}", decision.getStatus(), proposalId);
        return saved;
    }

    public UnderwritingDecision getDecisionByProposal(Long proposalId) {
        return decisionRepository.findByProposal_ProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found for proposal: " + proposalId));
    }

    private FinancialStatement computeRatios(FinancialStatement s) {
        try {
            if (s.getTotalAssets() != null && s.getTotalLiabilities() != null
                    && s.getTotalLiabilities().compareTo(BigDecimal.ZERO) != 0) {
                s.setCurrentRatio(s.getTotalAssets()
                        .divide(s.getTotalLiabilities(), 2, RoundingMode.HALF_UP));
            }
            if (s.getTotalLiabilities() != null && s.getNetWorth() != null
                    && s.getNetWorth().compareTo(BigDecimal.ZERO) != 0) {
                s.setDebtEquityRatio(s.getTotalLiabilities()
                        .divide(s.getNetWorth(), 2, RoundingMode.HALF_UP));
            }
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
