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

// Service for Collateral Management (4.5) and Facility Disbursement (4.6)
// These are linked - collateral is registered before facility is created
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

    // ── 4.5 Collateral Management ─────────────────────────────────

    // Register collateral - auto-computes realisable value
    public CollateralRecord registerCollateral(Long applicationId, CollateralRecord collateral) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        collateral.setApplication(application);

        // Auto-compute realisable value = marketValue * forceValuePercent / 100
        if (collateral.getMarketValue() != null && collateral.getForceValuePercent() != null) {
            BigDecimal realisable = collateral.getMarketValue()
                    .multiply(collateral.getForceValuePercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            collateral.setRealisableValue(realisable);
        }

        CollateralRecord saved = collateralRepository.save(collateral);
        log.info("Collateral registered for application {}: {} - {}", applicationId,
                saved.getAssetType(), saved.getMarketValue());
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
        CollateralRecord collateral = getCollateralById(collateralId);
        collateral.setStatus(status);
        log.info("Collateral {} status updated to {}", collateralId, status);
        return collateralRepository.save(collateral);
    }

    // Revalue a collateral asset - tracks change percentage
    public CollateralRevaluation revalueCollateral(Long collateralId, BigDecimal newValue, Long revaluedById) {
        CollateralRecord collateral = getCollateralById(collateralId);

        BigDecimal previousValue = collateral.getMarketValue();

        // Calculate % change
        BigDecimal changePercent = newValue.subtract(previousValue)
                .divide(previousValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        CollateralRevaluation revaluation = CollateralRevaluation.builder()
                .collateral(collateral)
                .revaluationDate(LocalDate.now())
                .previousValue(previousValue)
                .newValue(newValue)
                .revaluedById(revaluedById)
                .changePercent(changePercent)
                .build();

        // Update collateral market value
        collateral.setMarketValue(newValue);
        collateralRepository.save(collateral);

        log.info("Collateral {} revalued from {} to {} ({}%)",
                collateralId, previousValue, newValue, changePercent);
        return revaluationRepository.save(revaluation);
    }

    public List<CollateralRevaluation> getRevaluationHistory(Long collateralId) {
        return revaluationRepository.findByCollateral_CollateralId(collateralId);
    }

    // ── 4.6 Facility Disbursement ─────────────────────────────────

    // Create facility account after underwriting approval
    public FacilityAccount createFacility(Long applicationId, Long businessId, FacilityAccount facility) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        SMEBusiness business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));

        facility.setApplication(application);
        facility.setBusiness(business);
        facility.setDisbursedAmount(BigDecimal.ZERO);
        facility.setOutstandingBalance(BigDecimal.ZERO);

        FacilityAccount saved = facilityRepository.save(facility);
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

    // Process drawdown request - checks against sanctioned limit
    public Drawdown requestDrawdown(Long facilityId, Drawdown drawdown) {
        FacilityAccount facility = getFacilityById(facilityId);

        if (facility.getStatus() != FacilityStatus.ACTIVE) {
            throw new BadRequestException("Facility is not active: " + facilityId);
        }

        // Check if drawdown amount is within available limit
        BigDecimal available = facility.getSanctionedLimit()
                .subtract(facility.getDisbursedAmount());
        if (drawdown.getAmount().compareTo(available) > 0) {
            throw new BadRequestException("Drawdown amount exceeds available limit of " + available);
        }

        drawdown.setFacility(facility);
        drawdown.setStatus(DrawdownStatus.REQUESTED);

        Drawdown saved = drawdownRepository.save(drawdown);
        log.info("Drawdown requested for facility {}: amount={}", facilityId, drawdown.getAmount());
        return saved;
    }

    // Disburse a drawdown - updates facility balance
    public Drawdown disburseDrawdown(Long drawdownId) {
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));

        if (drawdown.getStatus() != DrawdownStatus.REQUESTED) {
            throw new BadRequestException("Only REQUESTED drawdowns can be disbursed");
        }

        drawdown.setStatus(DrawdownStatus.DISBURSED);
        drawdown.setDisbursedDate(LocalDate.now());

        // Update facility disbursed amount and outstanding balance
        FacilityAccount facility = drawdown.getFacility();
        facility.setDisbursedAmount(facility.getDisbursedAmount().add(drawdown.getAmount()));
        facility.setOutstandingBalance(facility.getOutstandingBalance().add(drawdown.getAmount()));
        facilityRepository.save(facility);

        log.info("Drawdown {} disbursed: amount={}", drawdownId, drawdown.getAmount());
        return drawdownRepository.save(drawdown);
    }

    public List<Drawdown> getDrawdownsByFacility(Long facilityId) {
        return drawdownRepository.findByFacility_FacilityId(facilityId);
    }

    // Record working capital utilisation for the period
    public WorkingCapitalUtilisation recordUtilisation(Long facilityId, WorkingCapitalUtilisation utilisation) {
        FacilityAccount facility = getFacilityById(facilityId);
        utilisation.setFacility(facility);

        // Auto-compute available limit and utilisation %
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
