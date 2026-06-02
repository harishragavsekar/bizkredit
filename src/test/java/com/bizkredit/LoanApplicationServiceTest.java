package com.bizkredit;

import com.bizkredit.entity.ApplicationDocument;
import com.bizkredit.entity.LoanApplication;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.DocumentType;
import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.ApplicationDocumentRepository;
import com.bizkredit.repository.LoanApplicationRepository;
import com.bizkredit.repository.SMEBusinessRepository;
import com.bizkredit.service.LoanApplicationService;
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
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private SMEBusinessRepository smeBusinessRepository;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private SMEBusiness sampleBusiness;
    private LoanApplication sampleApplication;

    @BeforeEach
    void setUp() {
        sampleBusiness = SMEBusiness.builder()
                .businessId(1L)
                .businessName("Subhishka Pvt Ltd")
                .registrationNumber("REG999")
                .build();

        sampleApplication = LoanApplication.builder()
                .applicationId(1L)
                .business(sampleBusiness)
                .productType(ProductType.TERM_LOAN)
                .requestedAmount(new BigDecimal("1000000"))
                .tenure(36)
                .purpose("Business expansion")
                .status(ApplicationStatus.SUBMITTED)
                .build();
    }

    @Test
    void submitApplication_success() {
        when(smeBusinessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(loanApplicationRepository.save(any())).thenReturn(sampleApplication);

        LoanApplication result = loanApplicationService.submitApplication(1L, sampleApplication);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(result.getBusiness().getBusinessId()).isEqualTo(1L);
        verify(loanApplicationRepository, times(1)).save(any());
    }

    @Test
    void submitApplication_businessNotFound_throwsResourceNotFound() {
        when(smeBusinessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.submitApplication(99L, sampleApplication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business not found");
    }

    @Test
    void getApplicationById_notFound_throwsResourceNotFound() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.getApplicationById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Application not found");
    }

    @Test
    void updateStatus_success() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(loanApplicationRepository.save(any())).thenReturn(sampleApplication);

        LoanApplication updated = loanApplicationService.updateStatus(1L, ApplicationStatus.IN_REVIEW);

        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.IN_REVIEW);
    }

    @Test
    void assignAnalyst_setsAnalystAndStatusInReview() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(loanApplicationRepository.save(any())).thenReturn(sampleApplication);

        LoanApplication result = loanApplicationService.assignAnalyst(1L, 5L);

        assertThat(result.getAssignedAnalystId()).isEqualTo(5L);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.IN_REVIEW);
    }

    @Test
    void uploadDocument_success() {
        ApplicationDocument doc = ApplicationDocument.builder()
                .documentType(DocumentType.AUDITED_FINANCIALS)
                .financialYear("2023-24")
                .filePath("/docs/financials.pdf")
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(documentRepository.save(any())).thenAnswer(inv -> {
            ApplicationDocument d = inv.getArgument(0);
            d.setDocId(1L);
            return d;
        });

        ApplicationDocument saved = loanApplicationService.uploadDocument(1L, doc);

        assertThat(saved.getApplication()).isEqualTo(sampleApplication);
        assertThat(saved.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void updateVerificationStatus_success() {
        ApplicationDocument doc = ApplicationDocument.builder()
                .docId(1L)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any())).thenReturn(doc);

        ApplicationDocument updated = loanApplicationService.updateVerificationStatus(1L, VerificationStatus.VERIFIED);

        assertThat(updated.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void getDocumentsByApplication_returnsList() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(documentRepository.findByApplication_ApplicationId(1L)).thenReturn(List.of());

        List<ApplicationDocument> result = loanApplicationService.getDocumentsByApplication(1L);

        assertThat(result).isEmpty();
        verify(documentRepository).findByApplication_ApplicationId(1L);
    }
}
