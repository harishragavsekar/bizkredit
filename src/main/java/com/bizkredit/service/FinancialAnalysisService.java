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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialStatementRepository statementRepository;
    private final CreditProposalRepository proposalRepository;
    private final UnderwritingDecisionRepository decisionRepository;
    private final LoanApplicationRepository applicationRepository;

    @Transactional
    public FinancialStatement addStatement(Long applicationId, FinancialStatement statement) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        statement.setApplication(application);
        statement = computeRatios(statement);

        FinancialStatement saved = statementRepository.save(statement);
        log.info("Financial statement added for application {}, year {}",
                applicationId, saved.getFinancialYear());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FinancialStatement> getStatementsByApplication(Long applicationId) {
        return statementRepository.findByApplication_ApplicationId(applicationId);
    }

    @Transactional
    public FinancialStatement verifyStatement(Long statementId) {
        FinancialStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));
        statement.setStatus("Verified");
        log.info("Statement {} verified", statementId);
        return statementRepository.save(statement);
    }

    @Transactional
    public CreditProposal createProposal(Long applicationId, CreditProposal proposal) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        proposal.setApplication(application);
        proposal.setStatus(ProposalStatus.DRAFT);
        CreditProposal saved = proposalRepository.save(proposal);
        log.info("Credit proposal created for application {}", applicationId);
        return saved;
    }

    @Transactional
    public CreditProposal submitProposal(Long proposalId) {
        CreditProposal proposal = getProposalById(proposalId);

        // Switch expression - only DRAFT can be submitted
        String message = switch (proposal.getStatus()) {
            case DRAFT -> null;
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

    @Transactional(readOnly = true)
    public CreditProposal getProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));
    }

    @Transactional(readOnly = true)
    public List<CreditProposal> getProposalsByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }

    @Transactional
    public UnderwritingDecision makeDecision(Long proposalId, UnderwritingDecision decision) {
        CreditProposal proposal = getProposalById(proposalId);

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new BadRequestException("Proposal must be SUBMITTED before a decision can be made");
        }

        decision.setProposal(proposal);

        // Switch expression - maps decision to proposal status
        ProposalStatus newProposalStatus = switch (decision.getStatus()) {
            case APPROVED, CONDITIONAL_APPROVAL -> ProposalStatus.APPROVED_BY_MANAGER;
            case DECLINED -> ProposalStatus.DECLINED;
        };

        proposal.setStatus(newProposalStatus);
        proposalRepository.save(proposal);

        UnderwritingDecision saved = decisionRepository.save(decision);
        log.info("Underwriting decision {} for proposal {}", decision.getStatus(), proposalId);
        return saved;
    }

    @Transactional(readOnly = true)
    public UnderwritingDecision getDecisionByProposal(Long proposalId) {
        return decisionRepository.findByProposal_ProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found for proposal: " + proposalId));
    }

    // Auto-computes 3 key financial ratios from raw data
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
