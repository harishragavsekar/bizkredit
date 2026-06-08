package com.bizkredit;

import com.bizkredit.entity.*;
import com.bizkredit.enums.*;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import com.bizkredit.service.CollateralFacilityService;
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
class CollateralFacilityServiceTest {

    @Mock private CollateralRecordRepository collateralRepository;
    @Mock private CollateralRevaluationRepository revaluationRepository;
    @Mock private FacilityAccountRepository facilityRepository;
    @Mock private DrawdownRepository drawdownRepository;
    @Mock private WorkingCapitalUtilisationRepository utilisationRepository;
    @Mock private LoanApplicationRepository applicationRepository;
    @Mock private SMEBusinessRepository businessRepository;

    @InjectMocks
    private CollateralFacilityService service;

    private LoanApplication sampleApplication;
    private SMEBusiness sampleBusiness;
    private CollateralRecord sampleCollateral;
    private FacilityAccount sampleFacility;

    @BeforeEach
    void setUp() {
        sampleApplication = LoanApplication.builder()
                .applicationId(1L)
                .productType(ProductType.TERM_LOAN)
                .status(ApplicationStatus.SANCTIONED)
                .build();

        sampleBusiness = SMEBusiness.builder()
                .businessId(1L)
                .businessName("Affrina Enterprises")
                .registrationNumber("REG456")
                .entityType(EntityType.PRIVATE_LIMITED)
                .build();

        sampleCollateral = CollateralRecord.builder()
                .collateralId(1L)
                .application(sampleApplication)
                .assetType(AssetType.PROPERTY)
                .marketValue(new BigDecimal("5000000"))
                .forceValuePercent(new BigDecimal("70"))
                .realisableValue(new BigDecimal("3500000"))
                .status(CollateralStatus.REGISTERED)
                .build();

        sampleFacility = FacilityAccount.builder()
                .facilityId(1L)
                .application(sampleApplication)
                .business(sampleBusiness)
                .productType(ProductType.TERM_LOAN)
                .sanctionedLimit(new BigDecimal("3000000"))
                .disbursedAmount(BigDecimal.ZERO)
                .outstandingBalance(BigDecimal.ZERO)
                .status(FacilityStatus.ACTIVE)
                .build();
    }

    @Test
    void registerCollateral_autoComputesRealisableValue() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(collateralRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CollateralRecord saved = service.registerCollateral(1L, sampleCollateral);

        // 5000000 * 70 / 100 = 3500000
        assertThat(saved.getRealisableValue()).isEqualByComparingTo(new BigDecimal("3500000.00"));
    }

    @Test
    void registerCollateral_applicationNotFound_throwsResourceNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerCollateral(99L, sampleCollateral))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createFacility_success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
        when(businessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(facilityRepository.save(any())).thenReturn(sampleFacility);

        FacilityAccount saved = service.createFacility(1L, 1L, sampleFacility);

        assertThat(saved.getStatus()).isEqualTo(FacilityStatus.ACTIVE);
        assertThat(saved.getDisbursedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void requestDrawdown_exceedsLimit_throwsBadRequest() {
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(sampleFacility));

        Drawdown drawdown = Drawdown.builder()
                .amount(new BigDecimal("5000000")) // more than sanctioned 3000000
                .purpose("Equipment purchase")
                .build();

        assertThatThrownBy(() -> service.requestDrawdown(1L, drawdown))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds available limit");
    }

    @Test
    void requestDrawdown_success() {
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(sampleFacility));
        Drawdown drawdown = Drawdown.builder()
                .amount(new BigDecimal("1000000"))
                .purpose("Working capital")
                .build();
        when(drawdownRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Drawdown saved = service.requestDrawdown(1L, drawdown);

        assertThat(saved.getStatus()).isEqualTo(DrawdownStatus.REQUESTED);
    }

    @Test
    void disburseDrawdown_success() {
        Drawdown drawdown = Drawdown.builder()
                .drawdownId(1L)
                .facility(sampleFacility)
                .amount(new BigDecimal("1000000"))
                .status(DrawdownStatus.REQUESTED)
                .build();

        when(drawdownRepository.findById(1L)).thenReturn(Optional.of(drawdown));
        when(drawdownRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Drawdown disbursed = service.disburseDrawdown(1L);

        assertThat(disbursed.getStatus()).isEqualTo(DrawdownStatus.DISBURSED);
        assertThat(disbursed.getDisbursedDate()).isNotNull();
    }

    @Test
    void disburseDrawdown_notRequested_throwsBadRequest() {
        Drawdown drawdown = Drawdown.builder()
                .drawdownId(1L)
                .facility(sampleFacility)
                .amount(new BigDecimal("1000000"))
                .status(DrawdownStatus.DISBURSED) // already disbursed
                .build();

        when(drawdownRepository.findById(1L)).thenReturn(Optional.of(drawdown));

        assertThatThrownBy(() -> service.disburseDrawdown(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("REQUESTED");
    }
}
