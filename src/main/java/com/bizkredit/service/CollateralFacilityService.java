package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.CollateralStatus;
import com.bizkredit.enums.DrawdownStatus;
import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Service for Collateral (4.5) and Facility Disbursement (4.6)
// Uses Java 21 features: var, switch expressions
@Slf4j
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

    // ── 4.5 Collateral ────────────────────────────────────────────

    public CollateralRecord registerCollateral(Long applicationId, CollateralRecord collateral) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        collateral.setApplication(application);

        if (collateral.getMarketValue() != null && collateral.getForceValuePercent() != null) {
            var realisable = collateral.getMarketValue()
                    .multiply(collateral.getForceValuePercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            collateral.setRealisableValue(realisable);
        }

        var saved = collateralRepository.save(collateral);
        log.info("Collateral registered for application {}: {}", applicationId, saved.getAssetType());
        return saved;
    }

    public CollateralRecord getCollateralById(Long collateralId) {
        return collateralRepository.findById(collateralId)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral not found: " + collateralId));
    }

    public List<CollateralRecord> getCollateralByApplication(Long applicationId) {
        return collateralRepository.findByApplication_ApplicationId(applicationId);
    }

    public CollateralRecord updateCollateralStatus(Long collateralId, CollateralStatus status) {
        var collateral = getCollateralById(collateralId);
        collateral.setStatus(status);
        log.info("Collateral {} status updated to {}", collateralId, status);
        return collateralRepository.save(collateral);
    }

    public CollateralRevaluation revalueCollateral(Long collateralId, BigDecimal newValue, Long revaluedById) {
        var collateral = getCollateralById(collateralId);
        var previousValue = collateral.getMarketValue();

        var changePercent = newValue.subtract(previousValue)
                .divide(previousValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        var revaluation = CollateralRevaluation.builder()
                .collateral(collateral)
                .revaluationDate(LocalDate.now())
                .previousValue(previousValue)
                .newValue(newValue)
                .revaluedById(revaluedById)
                .changePercent(changePercent)
                .build();

        collateral.setMarketValue(newValue);
        collateralRepository.save(collateral);

        log.info("Collateral {} revalued: {} -> {} ({}%)", collateralId, previousValue, newValue, changePercent);
        return revaluationRepository.save(revaluation);
    }

    public List<CollateralRevaluation> getRevaluationHistory(Long collateralId) {
        return revaluationRepository.findByCollateral_CollateralId(collateralId);
    }

    // ── 4.6 Facility ──────────────────────────────────────────────

    public FacilityAccount createFacility(Long applicationId, Long businessId, FacilityAccount facility) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));

        facility.setApplication(application);
        facility.setBusiness(business);
        facility.setDisbursedAmount(BigDecimal.ZERO);
        facility.setOutstandingBalance(BigDecimal.ZERO);

        var saved = facilityRepository.save(facility);
        log.info("Facility created for business {}: limit={}", businessId, saved.getSanctionedLimit());
        return saved;
    }

    public FacilityAccount getFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
    }

    public List<FacilityAccount> getFacilitiesByBusiness(Long businessId) {
        return facilityRepository.findByBusiness_BusinessId(businessId);
    }

    public Drawdown requestDrawdown(Long facilityId, Drawdown drawdown) {
        var facility = getFacilityById(facilityId);

        // Switch expression for facility status check
        var errorMessage = switch (facility.getStatus()) {
            case ACTIVE -> null; // can proceed
            case EXPIRED -> "Facility has expired";
            case CLOSED -> "Facility is closed";
            case NPA -> "Facility is classified as NPA";
        };

        if (errorMessage != null) throw new BadRequestException(errorMessage);

        var available = facility.getSanctionedLimit().subtract(facility.getDisbursedAmount());
        if (drawdown.getAmount().compareTo(available) > 0) {
            throw new BadRequestException("Drawdown amount exceeds available limit of " + available);
        }

        drawdown.setFacility(facility);
        drawdown.setStatus(DrawdownStatus.REQUESTED);

        var saved = drawdownRepository.save(drawdown);
        log.info("Drawdown requested for facility {}: amount={}", facilityId, drawdown.getAmount());
        return saved;
    }

    public Drawdown disburseDrawdown(Long drawdownId) {
        var drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));

        if (drawdown.getStatus() != DrawdownStatus.REQUESTED) {
            throw new BadRequestException("Only REQUESTED drawdowns can be disbursed");
        }

        drawdown.setStatus(DrawdownStatus.DISBURSED);
        drawdown.setDisbursedDate(LocalDate.now());

        var facility = drawdown.getFacility();
        facility.setDisbursedAmount(facility.getDisbursedAmount().add(drawdown.getAmount()));
        facility.setOutstandingBalance(facility.getOutstandingBalance().add(drawdown.getAmount()));
        facilityRepository.save(facility);

        log.info("Drawdown {} disbursed: amount={}", drawdownId, drawdown.getAmount());
        return drawdownRepository.save(drawdown);
    }

    public List<Drawdown> getDrawdownsByFacility(Long facilityId) {
        return drawdownRepository.findByFacility_FacilityId(facilityId);
    }

    public WorkingCapitalUtilisation recordUtilisation(Long facilityId, WorkingCapitalUtilisation utilisation) {
        var facility = getFacilityById(facilityId);
        utilisation.setFacility(facility);

        if (utilisation.getDrawingPower() != null && utilisation.getCurrentUtilisation() != null) {
            utilisation.setAvailableLimit(
                    utilisation.getDrawingPower().subtract(utilisation.getCurrentUtilisation()));

            if (utilisation.getDrawingPower().compareTo(BigDecimal.ZERO) != 0) {
                utilisation.setUtilisationPercent(
                        utilisation.getCurrentUtilisation()
                                .divide(utilisation.getDrawingPower(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")));
            }
        }

        return utilisationRepository.save(utilisation);
    }

    public List<WorkingCapitalUtilisation> getUtilisationByFacility(Long facilityId) {
        return utilisationRepository.findByFacility_FacilityId(facilityId);
    }
}
