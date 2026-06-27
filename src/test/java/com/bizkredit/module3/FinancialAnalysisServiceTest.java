package com.bizkredit.module3;

import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.module2.repository.LoanApplicationRepository;
import com.bizkredit.module3.entity.CreditProposal;
import com.bizkredit.module3.entity.FinancialStatement;
import com.bizkredit.module3.entity.UnderwritingDecision;
import com.bizkredit.module3.repository.CreditProposalRepository;
import com.bizkredit.module3.repository.FinancialStatementRepository;
import com.bizkredit.module3.repository.UnderwritingDecisionRepository;
import com.bizkredit.module3.service.FinancialAnalysisService;
import com.bizkredit.module3.service.ScorecardService;
import com.bizkredit.module1.service.AuditLogService;
import com.bizkredit.module5.service.NotificationHelper;
import com.bizkredit.common.enums.ApplicationStatus;
import com.bizkredit.common.enums.DecisionStatus;
import com.bizkredit.common.enums.NotificationCategory;
import com.bizkredit.common.enums.ProductType;
import com.bizkredit.common.enums.ProposalStatus;
import com.bizkredit.common.enums.RiskCategory;
import com.bizkredit.common.exception.BadRequestException;
import com.bizkredit.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalysisServiceTest {

    @Mock private FinancialStatementRepository statementRepository;
    @Mock private CreditProposalRepository proposalRepository;
    @Mock private UnderwritingDecisionRepository decisionRepository;
    @Mock private LoanApplicationRepository applicationRepository;
    @Mock private ScorecardService scorecardService;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationHelper notificationHelper;

    @InjectMocks
    private FinancialAnalysisService financialService;

    private LoanApplication sampleApplication;
    private CreditProposal sampleProposal;

    @BeforeEach
    void setUp() {
        sampleApplication = LoanApplication.builder()
                .applicationId(1L)
                .productType(ProductType.TERM_LOAN)
                .requestedAmount(new BigDecimal("1000000"))
                .status(ApplicationStatus.IN_REVIEW)
                .assignedAnalystId(10L)
                .build();

        sampleProposal = CreditProposal.builder()
                .proposalId(1L)
                .application(sampleApplication)
                .analystId(2L)
                .computedRatingScore(new BigDecimal("75.0"))
                .riskCategory(RiskCategory.MEDIUM)
                .suggestedAmount(new BigDecimal("900000"))
                .status(ProposalStatus.DRAFT)
                .scorecardAutoComputed(true)
                .build();
    }

    @Test
    void addStatement_autoComputesRatios() {
        FinancialStatement statement = FinancialStatement.builder()
                .financialYear("2023-24")
                .revenue(new BigDecimal("5000000"))
                .ebitda(new BigDecimal("800000"))
                .totalAssets(new BigDecimal("3000000"))
                .totalLiabilities(new BigDecimal("1500000"))
                .netWorth(new BigDecimal("1500000"))
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FinancialStatement saved = financialService.addStatement(1L, statement);

        assertThat(saved.getCurrentRatio()).isNotNull();
        assertThat(saved.getDebtEquityRatio()).isNotNull();
        assertThat(saved.getDscr()).isNotNull();
        verify(auditLogService).log(any(), eq("CREATE"), eq("FinancialStatement"), any());
    }

    @Test
    void addStatement_applicationNotFound_throwsResourceNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.addStatement(99L, new FinancialStatement()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProposal_success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(proposalRepository.save(any())).thenReturn(sampleProposal);

        CreditProposal saved = financialService.createProposal(1L, sampleProposal);

        assertThat(saved.getStatus()).isEqualTo(ProposalStatus.DRAFT);
        verify(auditLogService).log(any(), eq("CREATE"), eq("CreditProposal"), any());
    }

    @Test
    void submitProposal_success() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(proposalRepository.save(any())).thenReturn(sampleProposal);

        CreditProposal submitted = financialService.submitProposal(1L);

        assertThat(submitted.getStatus()).isEqualTo(ProposalStatus.SUBMITTED);
    }

    @Test
    void submitProposal_alreadySubmitted_throwsBadRequest() {
        sampleProposal.setStatus(ProposalStatus.SUBMITTED);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        assertThatThrownBy(() -> financialService.submitProposal(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void makeDecision_approved_updatesProposalStatus() {
        sampleProposal.setStatus(ProposalStatus.SUBMITTED);

        UnderwritingDecision decision = UnderwritingDecision.builder()
                .managerId(3L)
                .sanctionedAmount(new BigDecimal("900000"))
                .status(DecisionStatus.APPROVED)
                .build();

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(decisionRepository.save(any())).thenReturn(decision);

        UnderwritingDecision saved = financialService.makeDecision(1L, decision);

        assertThat(saved.getStatus()).isEqualTo(DecisionStatus.APPROVED);
        assertThat(sampleProposal.getStatus()).isEqualTo(ProposalStatus.APPROVED_BY_MANAGER);
        verify(notificationHelper).notify(
                eq(sampleApplication.getAssignedAnalystId()),
                contains("SANCTIONED"),
                eq(NotificationCategory.APPLICATION)
        );
    }

    @Test
    void makeDecision_proposalNotSubmitted_throwsBadRequest() {
        sampleProposal.setStatus(ProposalStatus.DRAFT);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        assertThatThrownBy(() -> financialService.makeDecision(1L, new UnderwritingDecision()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SUBMITTED");
    }
}
