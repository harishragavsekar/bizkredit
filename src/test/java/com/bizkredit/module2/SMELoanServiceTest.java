package com.bizkredit.module2;

import com.bizkredit.module2.entity.SMEBusiness;
import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.module2.entity.ApplicationDocument;
import com.bizkredit.module2.repository.SMEBusinessRepository;
import com.bizkredit.module2.repository.PromoterRepository;
import com.bizkredit.module2.repository.GroupCompanyRepository;
import com.bizkredit.module2.repository.LoanApplicationRepository;
import com.bizkredit.module2.repository.ApplicationDocumentRepository;
import com.bizkredit.module2.service.SMELoanService;
import com.bizkredit.module1.service.AuditLogService;
import com.bizkredit.module5.service.NotificationHelper;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.DocumentType;
import com.bizkredit.enums.EntityType;
import com.bizkredit.enums.ProductType;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SMELoanServiceTest {

    @Mock private SMEBusinessRepository businessRepository;
    @Mock private PromoterRepository promoterRepository;
    @Mock private GroupCompanyRepository groupCompanyRepository;
    @Mock private LoanApplicationRepository loanApplicationRepository;
    @Mock private ApplicationDocumentRepository documentRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationHelper notificationHelper;

    @InjectMocks
    private SMELoanService smeService;

    private SMEBusiness sampleBusiness;
    private LoanApplication sampleApplication;

    @BeforeEach
    void setUp() {
        sampleBusiness = SMEBusiness.builder()
                .businessId(1L)
                .businessName("Dileep Enterprises")
                .registrationNumber("REG123")
                .entityType(EntityType.PRIVATE_LIMITED)
                .status("Active")
                .build();

        sampleApplication = LoanApplication.builder()
                .applicationId(1L)
                .business(sampleBusiness)
                .productType(ProductType.TERM_LOAN)
                .requestedAmount(new BigDecimal("1000000"))
                .tenure(36)
                .status(ApplicationStatus.SUBMITTED)
                .assignedAnalystId(10L)
                .build();
    }

    @Test
    void registerBusiness_success() {
        when(businessRepository.existsByRegistrationNumber("REG123")).thenReturn(false);
        when(businessRepository.save(any())).thenReturn(sampleBusiness);

        SMEBusiness result = smeService.registerBusiness(sampleBusiness);

        assertThat(result.getBusinessName()).isEqualTo("Dileep Enterprises");
        verify(auditLogService).log(any(), eq("CREATE"), eq("SMEBusiness"), any());
    }

    @Test
    void registerBusiness_duplicate_throwsBadRequest() {
        when(businessRepository.existsByRegistrationNumber("REG123")).thenReturn(true);

        assertThatThrownBy(() -> smeService.registerBusiness(sampleBusiness))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void getBusinessById_notFound_throwsResourceNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> smeService.getBusinessById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitApplication_success() {
        when(businessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(loanApplicationRepository.save(any())).thenReturn(sampleApplication);

        LoanApplication result = smeService.submitApplication(1L, sampleApplication);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        verify(auditLogService).log(any(), eq("CREATE"), eq("LoanApplication"), any());
    }

    @Test
    void linkGroupCompany_sameId_throwsBadRequest() {
        assertThatThrownBy(() -> smeService.linkGroupCompany(1L, 1L, "Subsidiary"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    void assignAnalyst_setsStatusInReview() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(loanApplicationRepository.save(any())).thenReturn(sampleApplication);

        LoanApplication result = smeService.assignAnalyst(1L, 5L);

        assertThat(result.getAssignedAnalystId()).isEqualTo(5L);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.IN_REVIEW);
    }

    @Test
    void uploadDocument_success() {
        ApplicationDocument doc = ApplicationDocument.builder()
                .documentType(DocumentType.AUDITED_FINANCIALS)
                .financialYear("2023-24")
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationDocument saved = smeService.uploadDocument(1L, doc);

        assertThat(saved.getApplication()).isEqualTo(sampleApplication);
    }
}
