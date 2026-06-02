package com.bizkredit;

import com.bizkredit.entity.CreditProposal;
import com.bizkredit.entity.FinancialStatement;
import com.bizkredit.entity.LoanApplication;
import com.bizkredit.entity.UnderwritingDecision;
import com.bizkredit.enums.*;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.CreditProposalRepository;
import com.bizkredit.repository.FinancialStatementRepository;
import com.bizkredit.repository.LoanApplicationRepository;
import com.bizkredit.repository.UnderwritingDecisionRepository;
import com.bizkredit.service.FinancialAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAnalysisServiceTest {

    @Mock
    private FinancialStatementRepository financialStatementRepository;

    @Mock
    private CreditProposalRepository creditProposalRepository;

    @Mock
    private UnderwritingDecisionRepository underwritingDecisionRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private FinancialAnalysisService financialAnalysisService;

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
                .scorecardRating(new BigDecimal("75.0"))
                .riskCategory(RiskCategory.MEDIUM)
                .suggestedAmount(new BigDecimal("900000"))
                .suggestedRate(new BigDecimal("11.5"))
                .tenure(36)
                .analystRecommendation(AnalystRecommendation.APPROVE)
                .status(ProposalStatus.DRAFT)
                .build();
    }

    @Test
    void addFinancialStatement_success() {
        FinancialStatement statement = FinancialStatement.builder()
                .financialYear("2023-24")
                .revenue(new BigDecimal("5000000"))
                .ebitda(new BigDecimal("800000"))
                .totalAssets(new BigDecimal("3000000"))
                .totalLiabilities(new BigDecimal("1500000"))
                .netWorth(new BigDecimal("1500000"))
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(financialStatementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FinancialStatement saved = financialAnalysisService.addFinancialStatement(1L, statement);

        assertThat(saved.getCurrentRatio()).isNotNull();
        assertThat(saved.getDebtEquityRatio()).isNotNull();
        assertThat(saved.getDscr()).isNotNull();
        verify(financialStatementRepository).save(any());
    }

    @Test
    void addFinancialStatement_applicationNotFound_throwsResourceNotFound() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialAnalysisService.addFinancialStatement(99L, new FinancialStatement()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Application not found");
    }

    @Test
    void createProposal_success() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(creditProposalRepository.save(any())).thenReturn(sampleProposal);

        CreditProposal saved = financialAnalysisService.createProposal(1L, sampleProposal);

        assertThat(saved.getStatus()).isEqualTo(ProposalStatus.DRAFT);
        verify(creditProposalRepository).save(any());
    }

    @Test
    void submitProposal_success() {
        when(creditProposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(creditProposalRepository.save(any())).thenReturn(sampleProposal);

        CreditProposal submitted = financialAnalysisService.submitProposal(1L);

        assertThat(submitted.getStatus()).isEqualTo(ProposalStatus.SUBMITTED);
    }

    @Test
    void submitProposal_notDraft_throwsBadRequest() {
        sampleProposal.setStatus(ProposalStatus.SUBMITTED);
        when(creditProposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        assertThatThrownBy(() -> financialAnalysisService.submitProposal(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void makeDecision_approved_updatesProposalStatus() {
        sampleProposal.setStatus(ProposalStatus.SUBMITTED);

        UnderwritingDecision decision = UnderwritingDecision.builder()
                .managerId(3L)
                .sanctionedAmount(new BigDecimal("900000"))
                .approvedRate(new BigDecimal("11.5"))
                .tenure(36)
                .status(DecisionStatus.APPROVED)
                .build();

        when(creditProposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));
        when(underwritingDecisionRepository.save(any())).thenReturn(decision);

        UnderwritingDecision saved = financialAnalysisService.makeDecision(1L, decision);

        assertThat(saved.getStatus()).isEqualTo(DecisionStatus.APPROVED);
        assertThat(sampleProposal.getStatus()).isEqualTo(ProposalStatus.APPROVED_BY_MANAGER);
    }

    @Test
    void makeDecision_proposalNotSubmitted_throwsBadRequest() {
        sampleProposal.setStatus(ProposalStatus.DRAFT);
        when(creditProposalRepository.findById(1L)).thenReturn(Optional.of(sampleProposal));

        assertThatThrownBy(() -> financialAnalysisService.makeDecision(1L, new UnderwritingDecision()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SUBMITTED");
    }

    @Test
    void getStatementsByApplication_returnsList() {
        when(financialStatementRepository.findByApplication_ApplicationId(1L)).thenReturn(List.of());

        List<FinancialStatement> result = financialAnalysisService.getStatementsByApplication(1L);

        assertThat(result).isEmpty();
        verify(financialStatementRepository).findByApplication_ApplicationId(1L);
    }
}
