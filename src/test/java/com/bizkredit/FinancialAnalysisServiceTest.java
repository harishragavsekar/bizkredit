package com.bizkredit;

import com.bizkredit.entity.*;
import com.bizkredit.enums.*;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import com.bizkredit.service.FinancialAnalysisService;
import com.bizkredit.service.ScorecardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAnalysisServiceTest {

    @Mock private FinancialStatementRepository statementRepository;
    @Mock private CreditProposalRepository proposalRepository;
    @Mock private UnderwritingDecisionRepository decisionRepository;
    @Mock private LoanApplicationRepository applicationRepository;
    @Mock private ScorecardService scorecardService;

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
                .build();

        sampleProposal = CreditProposal.builder()
                .proposalId(1L)
                .application(sampleApplication)
                .analystId(2L)
                .computedRatingScore(new BigDecimal("75.0"))
                .riskCategory(RiskCategory.MEDIUM)
                .suggestedAmount(new BigDecimal("900000"))
                .status(ProposalStatus.DRAFT)
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

        // Service now returns specific message per status
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
