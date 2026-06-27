package com.bizkredit.module4.service;

import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.module2.entity.SMEBusiness;
import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.module4.entity.CollateralRecord;
import com.bizkredit.module4.entity.CollateralRevaluation;
import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.module4.entity.WorkingCapitalUtilisation;
import com.bizkredit.module2.repository.LoanApplicationRepository;
import com.bizkredit.module2.repository.SMEBusinessRepository;
import com.bizkredit.module4.repository.FacilityAccountRepository;
import com.bizkredit.module4.repository.CollateralRecordRepository;
import com.bizkredit.module4.repository.CollateralRevaluationRepository;
import com.bizkredit.module4.repository.DrawdownRepository;
import com.bizkredit.module4.repository.WorkingCapitalUtilisationRepository;
import com.bizkredit.common.enums.DrawdownStatus;
import com.bizkredit.common.exception.BadRequestException;
import com.bizkredit.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollateralFacilityService {

    private final CollateralRecordRepository collateralRepository;
    private final CollateralRevaluationRepository revaluationRepository;
    private final FacilityAccountRepository facilityRepository;
    private final DrawdownRepository drawdownRepository;
    private final WorkingCapitalUtilisationRepository utilisationRepository;
    private final LoanApplicationRepository applicationRepository;
    private final SMEBusinessRepository businessRepository;

    @Transactional
    public CollateralRecord registerCollateral(Long applicationId, CollateralRecord collateral) {
        collateral.setApplication(getApplication(applicationId));
        setRealisableValue(collateral);
        return collateralRepository.save(collateral);
    }

    public CollateralRecord getCollateralById(Long collateralId) {
        return collateralRepository.findById(collateralId)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral not found"));
    }

    @Transactional
    public CollateralRecord updateCollateral(Long collateralId, CollateralRecord updates) {
        CollateralRecord collateral = getCollateralById(collateralId);

        if (updates.getDescription() != null) collateral.setDescription(updates.getDescription());
        if (updates.getOwnerName() != null) collateral.setOwnerName(updates.getOwnerName());
        if (updates.getMarketValue() != null) collateral.setMarketValue(updates.getMarketValue());
        if (updates.getForceValuePercent() != null) collateral.setForceValuePercent(updates.getForceValuePercent());

        setRealisableValue(collateral);
        return collateralRepository.save(collateral);
    }

    @Transactional
    public CollateralRevaluation revalueCollateral(Long collateralId, BigDecimal newValue, Long revaluedById) {
        CollateralRecord collateral = getCollateralById(collateralId);
        BigDecimal oldValue = collateral.getMarketValue();

        if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("Invalid previous market value");
        }

        collateral.setMarketValue(newValue);
        setRealisableValue(collateral);
        collateralRepository.save(collateral);

        return revaluationRepository.save(CollateralRevaluation.builder()
                .collateral(collateral)
                .revaluationDate(LocalDate.now())
                .previousValue(oldValue)
                .newValue(newValue)
                .revaluedById(revaluedById)
                .changePercent(newValue.subtract(oldValue)
                        .divide(oldValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")))
                .build());
    }

    @Transactional
    public FacilityAccount createFacility(Long applicationId, Long businessId, FacilityAccount facility) {
        facility.setApplication(getApplication(applicationId));
        facility.setBusiness(getBusiness(businessId));
        facility.setDisbursedAmount(BigDecimal.ZERO);
        facility.setOutstandingBalance(BigDecimal.ZERO);
        return facilityRepository.save(facility);
    }

    public FacilityAccount getFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found"));
    }

    @Transactional
    public FacilityAccount updateFacility(Long facilityId, FacilityAccount updates) {
        FacilityAccount facility = getFacilityById(facilityId);

        if (updates.getExpiryDate() != null) facility.setExpiryDate(updates.getExpiryDate());
        if (updates.getInterestRate() != null) facility.setInterestRate(updates.getInterestRate());

        return facilityRepository.save(facility);
    }

    @Transactional
    public Drawdown requestDrawdown(Long facilityId, Drawdown drawdown) {
        FacilityAccount facility = getFacilityById(facilityId);
        BigDecimal available = facility.getSanctionedLimit().subtract(facility.getDisbursedAmount());

        if (drawdown.getAmount().compareTo(available) > 0) {
            throw new BadRequestException("Drawdown amount exceeds available limit");
        }

        drawdown.setFacility(facility);
        drawdown.setStatus(DrawdownStatus.REQUESTED);
        return drawdownRepository.save(drawdown);
    }

    public Drawdown getDrawdownById(Long drawdownId) {
        return drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found"));
    }

    @Transactional
    public Drawdown disburseDrawdown(Long drawdownId) {
        Drawdown drawdown = getDrawdownById(drawdownId);
        FacilityAccount facility = drawdown.getFacility();

        drawdown.setStatus(DrawdownStatus.DISBURSED);
        drawdown.setDisbursedDate(LocalDate.now());

        facility.setDisbursedAmount(facility.getDisbursedAmount().add(drawdown.getAmount()));
        facility.setOutstandingBalance(facility.getOutstandingBalance().add(drawdown.getAmount()));

        facilityRepository.save(facility);
        return drawdownRepository.save(drawdown);
    }

    @Transactional
    public WorkingCapitalUtilisation recordUtilisation(Long facilityId, WorkingCapitalUtilisation utilisation) {
        utilisation.setFacility(getFacilityById(facilityId));
        setUtilisationValues(utilisation);
        return utilisationRepository.save(utilisation);
    }

    public List<WorkingCapitalUtilisation> getUtilisationByFacility(Long facilityId) {
        return utilisationRepository.findByFacility_FacilityId(facilityId);
    }

    private LoanApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private SMEBusiness getBusiness(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
    }

    private void setRealisableValue(CollateralRecord collateral) {
        if (collateral.getMarketValue() != null && collateral.getForceValuePercent() != null) {
            collateral.setRealisableValue(
                    collateral.getMarketValue()
                            .multiply(collateral.getForceValuePercent())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        }
    }

    private void setUtilisationValues(WorkingCapitalUtilisation utilisation) {
        if (utilisation.getDrawingPower() != null && utilisation.getCurrentUtilisation() != null) {
            utilisation.setAvailableLimit(utilisation.getDrawingPower().subtract(utilisation.getCurrentUtilisation()));

            if (utilisation.getDrawingPower().compareTo(BigDecimal.ZERO) != 0) {
                utilisation.setUtilisationPercent(
                        utilisation.getCurrentUtilisation()
                                .divide(utilisation.getDrawingPower(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")));
            }
        }
    }
}