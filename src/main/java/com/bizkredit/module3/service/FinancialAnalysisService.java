package com.bizkredit.module3.service;

import com.bizkredit.common.enums.ApplicationStatus;
import com.bizkredit.common.enums.NotificationCategory;
import com.bizkredit.common.enums.ProposalStatus;
import com.bizkredit.module1.service.AuditLogService;
import com.bizkredit.module5.service.NotificationHelper;
import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.module3.entity.FinancialStatement;
import com.bizkredit.module3.entity.CreditProposal;
import com.bizkredit.module3.entity.UnderwritingDecision;
import com.bizkredit.module2.repository.LoanApplicationRepository;
import com.bizkredit.module3.repository.FinancialStatementRepository;
import com.bizkredit.module3.repository.CreditProposalRepository;
import com.bizkredit.module3.repository.UnderwritingDecisionRepository;
import com.bizkredit.common.exception.BadRequestException;
import com.bizkredit.common.exception.ResourceNotFoundException;
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
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;
    private final ScorecardService scorecardService;

    @Transactional
    public FinancialStatement addStatement(Long applicationId, FinancialStatement statement) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        statement.setApplication(application);
        statement = computeRatios(statement);
        FinancialStatement saved = statementRepository.save(statement);
        auditLogService.log(null, "CREATE", "FinancialStatement", String.valueOf(saved.getStatementId()));
        log.info("Financial statement added for application {}, year {}", applicationId, saved.getFinancialYear());
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
        auditLogService.log(null, "STATUS_CHANGE", "FinancialStatement", String.valueOf(statementId));
        log.info("Statement {} verified", statementId);
        return statementRepository.save(statement);
    }

    @Transactional
    public CreditProposal createProposal(Long applicationId, CreditProposal proposal) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        proposal.setApplication(application);
        proposal.setStatus(ProposalStatus.DRAFT);
        applyAutoScorecard(proposal, application);
        CreditProposal saved = proposalRepository.save(proposal);
        auditLogService.log(null, "CREATE", "CreditProposal", String.valueOf(saved.getProposalId()));
        log.info("Credit proposal created for application {}", applicationId);
        return saved;
    }

    /**
     * Looks up the active scorecard for the application's product type and runs it,
     * populating computedRatingScore/riskCategory/computedScore/scorecardId on the proposal.
     * No-op (leaves fields as the analyst left them) if no ACTIVE scorecard exists yet
     * for the product, so proposal creation is never blocked on scorecard setup.
     */
    private void applyAutoScorecard(CreditProposal proposal, LoanApplication application) {
        var scorecard = scorecardService.findActiveScorecardFor(application.getProductType());
        if (scorecard == null) {
            log.info("No ACTIVE scorecard for product type {} - skipping auto-score for application {}",
                    application.getProductType(), application.getApplicationId());
            return;
        }
        try {
            var result = scorecardService.computeForApplication(scorecard, application.getApplicationId());
            proposal.setScorecardId(scorecard.getScorecardId());
            proposal.setComputedScore(result.computedScore());
            proposal.setRatingLabel(result.rating());
            if (result.rating() != null) {
                proposal.setComputedRatingScore(new BigDecimal(result.computedScore()));
            }
            proposal.setRiskCategory(result.riskCategory());
            proposal.setScorecardAutoComputed(true);
            if (result.partialData()) {
                log.warn("Auto-score for application {} used partial data (weight applied: {})",
                        application.getApplicationId(), result.totalWeightApplied());
            }
        } catch (Exception e) {
            // Never let a scoring failure block proposal creation/submission - log and move on,
            // the analyst can still set the rating manually.
            log.error("Auto-scoring failed for application {}: {}", application.getApplicationId(), e.getMessage());
        }
    }

    // PUT /api/financial/proposals/{id} - update (Draft only)
    @Transactional
    public CreditProposal updateProposal(Long proposalId, CreditProposal updates) {
        CreditProposal existing = getProposalById(proposalId);
        if (existing.getStatus() != ProposalStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT proposals can be updated");
        }
        if (updates.getRatingLabel() != null) {
            existing.setRatingLabel(updates.getRatingLabel());
            // An explicit manual rating means the analyst is overriding the auto-score -
            // don't let a later submit() silently recompute and replace it.
            existing.setScorecardAutoComputed(false);
        }
        if (updates.getRiskCategory() != null) {
            existing.setRiskCategory(updates.getRiskCategory());
            existing.setScorecardAutoComputed(false);
        }
        if (updates.getSuggestedAmount() != null) existing.setSuggestedAmount(updates.getSuggestedAmount());
        if (updates.getSuggestedRate() != null) existing.setSuggestedRate(updates.getSuggestedRate());
        if (updates.getTenure() != null) existing.setTenure(updates.getTenure());
        if (updates.getConditions() != null) existing.setConditions(updates.getConditions());
        if (updates.getAnalystRecommendation() != null) existing.setAnalystRecommendation(updates.getAnalystRecommendation());
        auditLogService.log(null, "UPDATE", "CreditProposal", String.valueOf(proposalId));
        return proposalRepository.save(existing);
    }

    @Transactional
    public CreditProposal submitProposal(Long proposalId) {
        CreditProposal proposal = getProposalById(proposalId);
        String message = switch (proposal.getStatus()) {
            case DRAFT -> null;
            case SUBMITTED -> "Proposal already submitted";
            case APPROVED_BY_MANAGER -> "Proposal already approved";
            case DECLINED -> "Proposal was declined";
            case SANCTIONED -> "Proposal already sanctioned";
        };
        if (message != null) throw new BadRequestException(message);

        // Re-score on submit (only if the analyst hasn't manually overridden the rating)
        // so the submitted proposal reflects the latest financial statement / KYC data,
        // not whatever was on hand at creation time.
        if (proposal.isScorecardAutoComputed() && proposal.getApplication() != null) {
            applyAutoScorecard(proposal, proposal.getApplication());
        }

        proposal.setStatus(ProposalStatus.SUBMITTED);
        auditLogService.log(null, "STATUS_CHANGE", "CreditProposal", String.valueOf(proposalId));
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

        // Validate SanctionedAmount <= SuggestedAmount
        if (decision.getSanctionedAmount() != null && proposal.getSuggestedAmount() != null) {
            if (decision.getSanctionedAmount().compareTo(proposal.getSuggestedAmount()) > 0) {
                throw new BadRequestException("Sanctioned amount cannot exceed suggested amount of "
                        + proposal.getSuggestedAmount());
            }
        }

        decision.setProposal(proposal);

        ProposalStatus newProposalStatus = switch (decision.getStatus()) {
            case APPROVED, CONDITIONAL_APPROVAL -> ProposalStatus.APPROVED_BY_MANAGER;
            case DECLINED -> ProposalStatus.DECLINED;
        };

        proposal.setStatus(newProposalStatus);
        proposalRepository.save(proposal);

        // Auto-transition LoanApplication on decision
        LoanApplication application = proposal.getApplication();
        if (application != null) {
            ApplicationStatus newAppStatus = switch (decision.getStatus()) {
                case APPROVED, CONDITIONAL_APPROVAL -> ApplicationStatus.SANCTIONED;
                case DECLINED -> ApplicationStatus.REJECTED;
            };
            application.setStatus(newAppStatus);
            applicationRepository.save(application);

            // Notify assigned analyst of the decision
            if (application.getAssignedAnalystId() != null) {
                notificationHelper.notify(application.getAssignedAnalystId(),
                        "Application #" + application.getApplicationId() + " has been "
                                + newAppStatus.name() + " by underwriting",
                        NotificationCategory.APPLICATION);
            }
        }

        UnderwritingDecision saved = decisionRepository.save(decision);
        auditLogService.log(null, "APPROVE", "UnderwritingDecision", String.valueOf(saved.getDecisionId()));
        log.info("Underwriting decision {} for proposal {}", decision.getStatus(), proposalId);
        return saved;
    }

    @Transactional(readOnly = true)
    public UnderwritingDecision getDecisionByProposal(Long proposalId) {
        return decisionRepository.findByProposal_ProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found for proposal: " + proposalId));
    }

    // GET statement by ID
    @Transactional(readOnly = true)
    public FinancialStatement getStatementById(Long id) {
        return statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + id));
    }

    // UPDATE statement (Draft only)
    @Transactional
    public FinancialStatement updateStatement(Long id, FinancialStatement updates) {
        FinancialStatement existing = statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + id));
        if (updates.getRevenue() != null) existing.setRevenue(updates.getRevenue());
        if (updates.getEbitda() != null) existing.setEbitda(updates.getEbitda());
        if (updates.getPat() != null) existing.setPat(updates.getPat());
        if (updates.getTotalAssets() != null) existing.setTotalAssets(updates.getTotalAssets());
        if (updates.getTotalLiabilities() != null) existing.setTotalLiabilities(updates.getTotalLiabilities());
        // Recompute
        if (existing.getTotalAssets() != null && existing.getTotalLiabilities() != null) {
            existing.setNetWorth(existing.getTotalAssets().subtract(existing.getTotalLiabilities()));
        }
        return statementRepository.save(computeRatios(existing));
    }

    // GET proposals by application
    @Transactional(readOnly = true)
    public List<CreditProposal> getProposalsByApplication(Long applicationId) {
        return proposalRepository.findAllByApplication_ApplicationId(applicationId);
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
