package com.bizkredit;

import com.bizkredit.entity.Promoter;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.enums.EntityType;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.GroupCompanyRepository;
import com.bizkredit.repository.PromoterRepository;
import com.bizkredit.repository.SMEBusinessRepository;
import com.bizkredit.service.SMEBusinessService;
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
class SMEBusinessServiceTest {

    @Mock
    private SMEBusinessRepository smeBusinessRepository;

    @Mock
    private PromoterRepository promoterRepository;

    @Mock
    private GroupCompanyRepository groupCompanyRepository;

    @InjectMocks
    private SMEBusinessService smeBusinessService;

    private SMEBusiness sampleBusiness;

    @BeforeEach
    void setUp() {
        sampleBusiness = SMEBusiness.builder()
                .businessId(1L)
                .businessName("Dileep Enterprises")
                .registrationNumber("REG123456")
                .entityType(EntityType.PRIVATE_LIMITED)
                .industry("Manufacturing")
                .annualTurnover(new BigDecimal("5000000"))
                .status("Active")
                .kycStatus("Pending")
                .build();
    }

    @Test
    void registerBusiness_success() {
        when(smeBusinessRepository.existsByRegistrationNumber("REG123456")).thenReturn(false);
        when(smeBusinessRepository.save(any(SMEBusiness.class))).thenReturn(sampleBusiness);

        SMEBusiness result = smeBusinessService.registerBusiness(sampleBusiness);

        assertThat(result.getBusinessName()).isEqualTo("Dileep Enterprises");
        assertThat(result.getStatus()).isEqualTo("Active");
        verify(smeBusinessRepository, times(1)).save(any());
    }

    @Test
    void registerBusiness_duplicate_throwsBadRequest() {
        when(smeBusinessRepository.existsByRegistrationNumber("REG123456")).thenReturn(true);

        assertThatThrownBy(() -> smeBusinessService.registerBusiness(sampleBusiness))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(smeBusinessRepository, never()).save(any());
    }

    @Test
    void getBusinessById_notFound_throwsResourceNotFound() {
        when(smeBusinessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> smeBusinessService.getBusinessById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business not found");
    }

    @Test
    void updateKycStatus_success() {
        when(smeBusinessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(smeBusinessRepository.save(any())).thenReturn(sampleBusiness);

        SMEBusiness updated = smeBusinessService.updateKycStatus(1L, "Verified");

        assertThat(updated.getKycStatus()).isEqualTo("Verified");
    }

    @Test
    void addPromoter_success() {
        Promoter promoter = Promoter.builder()
                .name("Dileep Kumar")
                .shareholdingPercent(new BigDecimal("60.00"))
                .creditScore(750)
                .build();

        when(smeBusinessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(promoterRepository.save(any(Promoter.class))).thenAnswer(inv -> {
            Promoter p = inv.getArgument(0);
            p.setPromoterId(1L);
            return p;
        });

        Promoter saved = smeBusinessService.addPromoter(1L, promoter);

        assertThat(saved.getBusiness()).isEqualTo(sampleBusiness);
        assertThat(saved.getName()).isEqualTo("Dileep Kumar");
    }

    @Test
    void linkGroupCompany_sameBusinessId_throwsBadRequest() {
        when(smeBusinessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));

        assertThatThrownBy(() -> smeBusinessService.linkGroupCompany(1L, 1L, "Subsidiary"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    void getPromotersByBusiness_returnsList() {
        when(smeBusinessRepository.findById(1L)).thenReturn(Optional.of(sampleBusiness));
        when(promoterRepository.findByBusiness_BusinessId(1L)).thenReturn(List.of());

        List<Promoter> result = smeBusinessService.getPromotersByBusiness(1L);

        assertThat(result).isEmpty();
        verify(promoterRepository).findByBusiness_BusinessId(1L);
    }
}
