package com.bizkredit.module5;

import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.module4.repository.DrawdownRepository;
import com.bizkredit.module4.repository.FacilityAccountRepository;
import com.bizkredit.module5.entity.EarlyWarningSignal;
import com.bizkredit.module5.entity.NPARecord;
import com.bizkredit.module5.repository.EarlyWarningSignalRepository;
import com.bizkredit.module5.repository.NPARecordRepository;
import com.bizkredit.module5.service.NPAClassificationService;
import com.bizkredit.enums.DrawdownStatus;
import com.bizkredit.enums.EWSStatus;
import com.bizkredit.enums.EWSSeverity;
import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import com.bizkredit.enums.ProductType;
import com.bizkredit.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NPAClassificationServiceTest {

    @Mock private FacilityAccountRepository facilityRepository;
    @Mock private DrawdownRepository drawdownRepository;
    @Mock private NPARecordRepository npaRecordRepository;
    @Mock private EarlyWarningSignalRepository ewsRepository;

    @InjectMocks
    private NPAClassificationService service;

    private FacilityAccount sampleFacility;

    @BeforeEach
    void setUp() {
        sampleFacility = FacilityAccount.builder()
                .facilityId(1L)
                .productType(ProductType.TERM_LOAN)
                .sanctionedLimit(new BigDecimal("2000000"))
                .outstandingBalance(new BigDecimal("1500000"))
                .status(FacilityStatus.ACTIVE)
                .build();
    }

    @Test
    void runClassification_noOverdueDrawdowns_classifiesNothing() {
        when(facilityRepository.findByStatus(FacilityStatus.ACTIVE)).thenReturn(List.of(sampleFacility));
        when(drawdownRepository.findByFacility_FacilityId(1L)).thenReturn(List.of());

        int count = service.runClassification();

        assertThat(count).isZero();
        verify(npaRecordRepository, never()).save(any());
    }

    @Test
    void runClassification_over90Days_classifiesAsNPA() {
        Drawdown overdueDrawdown = Drawdown.builder()
                .drawdownId(1L)
                .facility(sampleFacility)
                .amount(new BigDecimal("1000000"))
                .status(DrawdownStatus.OVERDUE)
                .repaymentDate(LocalDate.now().minusDays(100))
                .build();

        when(facilityRepository.findByStatus(FacilityStatus.ACTIVE)).thenReturn(List.of(sampleFacility));
        when(drawdownRepository.findByFacility_FacilityId(1L)).thenReturn(List.of(overdueDrawdown));
        when(npaRecordRepository.findByFacility_FacilityIdAndStatus(1L, NPARecordStatus.ACTIVE))
                .thenReturn(Optional.empty());

        int count = service.runClassification();

        assertThat(count).isEqualTo(1);
        assertThat(sampleFacility.getStatus()).isEqualTo(FacilityStatus.NPA);
        verify(npaRecordRepository).save(any(NPARecord.class));
    }

    @Test
    void runClassification_30to60Days_createsAmberEWSSignal() {
        Drawdown overdueDrawdown = Drawdown.builder()
                .drawdownId(2L)
                .facility(sampleFacility)
                .amount(new BigDecimal("500000"))
                .status(DrawdownStatus.OVERDUE)
                .repaymentDate(LocalDate.now().minusDays(45))
                .build();

        when(facilityRepository.findByStatus(FacilityStatus.ACTIVE)).thenReturn(List.of(sampleFacility));
        when(drawdownRepository.findByFacility_FacilityId(1L)).thenReturn(List.of(overdueDrawdown));
        when(ewsRepository.findByFacility_FacilityId(1L)).thenReturn(List.of());

        int count = service.runClassification();

        assertThat(count).isZero();
        verify(ewsRepository).save(any(EarlyWarningSignal.class));
        verify(npaRecordRepository, never()).save(any());
    }

    @Test
    void runClassification_existingNPARecord_skipsReclassification() {
        Drawdown overdueDrawdown = Drawdown.builder()
                .drawdownId(3L)
                .facility(sampleFacility)
                .amount(new BigDecimal("1000000"))
                .status(DrawdownStatus.OVERDUE)
                .repaymentDate(LocalDate.now().minusDays(95))
                .build();

        NPARecord existingRecord = NPARecord.builder()
                .npaId(1L)
                .facility(sampleFacility)
                .status(NPARecordStatus.ACTIVE)
                .provisioningCategory(NPAProvisioningCategory.SUB_STANDARD)
                .build();

        when(facilityRepository.findByStatus(FacilityStatus.ACTIVE)).thenReturn(List.of(sampleFacility));
        when(drawdownRepository.findByFacility_FacilityId(1L)).thenReturn(List.of(overdueDrawdown));
        when(npaRecordRepository.findByFacility_FacilityIdAndStatus(1L, NPARecordStatus.ACTIVE))
                .thenReturn(Optional.of(existingRecord));

        int count = service.runClassification();

        assertThat(count).isZero();
        verify(npaRecordRepository, never()).save(any());
    }

    @Test
    void upgradeNPA_success_setsStatusUpgradedAndReactivatesFacility() {
        NPARecord npaRecord = NPARecord.builder()
                .npaId(1L)
                .facility(sampleFacility)
                .status(NPARecordStatus.ACTIVE)
                .provisioningCategory(NPAProvisioningCategory.SUB_STANDARD)
                .build();
        sampleFacility.setStatus(FacilityStatus.NPA);

        when(npaRecordRepository.findById(1L)).thenReturn(Optional.of(npaRecord));
        when(npaRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NPARecord upgraded = service.upgradeNPA(1L);

        assertThat(upgraded.getStatus()).isEqualTo(NPARecordStatus.UPGRADED);
        assertThat(sampleFacility.getStatus()).isEqualTo(FacilityStatus.ACTIVE);
        verify(facilityRepository).save(sampleFacility);
    }

    @Test
    void upgradeNPA_notFound_throwsResourceNotFoundException() {
        when(npaRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upgradeNPA(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("NPA record not found");
    }
}
