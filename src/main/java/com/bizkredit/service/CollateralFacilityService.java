package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.*;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

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
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;

    // ── 4.5 Collateral ────────────────────────────────────────────

    @Transactional
    public CollateralRecord registerCollateral(Long applicationId, CollateralRecord collateral) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        collateral.setApplication(application);
        if (collateral.getMarketValue() != null && collateral.getForceValuePercent() != null) {
            BigDecimal realisable = collateral.getMarketValue()
                    .multiply(collateral.getForceValuePercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            collateral.setRealisableValue(realisable);
        }
        CollateralRecord saved = collateralRepository.save(collateral);
        auditLogService.log(null, "CREATE", "CollateralRecord", String.valueOf(saved.getCollateralId()));
        log.info("Collateral registered for application {}: {}", applicationId, saved.getAssetType());
        return saved;
    }

    @Transactional(readOnly = true)
    public CollateralRecord getCollateralById(Long collateralId) {
        return collateralRepository.findById(collateralId)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral not found: " + collateralId));
    }

    @Transactional(readOnly = true)
    public List<CollateralRecord> getCollateralByApplication(Long applicationId) {
        return collateralRepository.findByApplication_ApplicationId(applicationId);
    }

    @Transactional
    public CollateralRecord updateCollateralStatus(Long collateralId, CollateralStatus status) {
        CollateralRecord collateral = getCollateralById(collateralId);
        collateral.setStatus(status);
        auditLogService.log(null, "STATUS_CHANGE", "CollateralRecord", String.valueOf(collateralId));
        log.info("Collateral {} status updated to {}", collateralId, status);
        return collateralRepository.save(collateral);
    }

    @Transactional
    public CollateralRevaluation revalueCollateral(Long collateralId, BigDecimal newValue, Long revaluedById) {
        CollateralRecord collateral = getCollateralById(collateralId);
        BigDecimal previousValue = collateral.getMarketValue();
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
        collateral.setMarketValue(newValue);
        collateralRepository.save(collateral);
        auditLogService.log(revaluedById, "UPDATE", "CollateralRecord", String.valueOf(collateralId));
        log.info("Collateral {} revalued: {} -> {} ({}%)", collateralId, previousValue, newValue, changePercent);
        return revaluationRepository.save(revaluation);
    }

    @Transactional(readOnly = true)
    public List<CollateralRevaluation> getRevaluationHistory(Long collateralId) {
        return revaluationRepository.findByCollateral_CollateralId(collateralId);
    }

    // ── 4.6 Facility ──────────────────────────────────────────────

    @Transactional
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
        auditLogService.log(null, "CREATE", "FacilityAccount", String.valueOf(saved.getFacilityId()));
        log.info("Facility created for business {}: limit={}", businessId, saved.getSanctionedLimit());
        return saved;
    }

    @Transactional(readOnly = true)
    public FacilityAccount getFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
    }

    @Transactional(readOnly = true)
    public List<FacilityAccount> getFacilitiesByBusiness(Long businessId) {
        return facilityRepository.findByBusiness_BusinessId(businessId);
    }

    // GET /api/facilities with filters
    @Transactional(readOnly = true)
    public List<FacilityAccount> getFacilitiesFiltered(Long businessId, FacilityStatus status, ProductType productType) {
        return facilityRepository.findWithFilters(businessId, status, productType);
    }

    // PUT /api/facilities/{id} — update ExpiryDate or InterestRate
    @Transactional
    public FacilityAccount updateFacility(Long facilityId, FacilityAccount updates) {
        FacilityAccount existing = getFacilityById(facilityId);
        if (updates.getExpiryDate() != null) existing.setExpiryDate(updates.getExpiryDate());
        if (updates.getInterestRate() != null) existing.setInterestRate(updates.getInterestRate());
        auditLogService.log(null, "UPDATE", "FacilityAccount", String.valueOf(facilityId));
        return facilityRepository.save(existing);
    }

    // PATCH /api/facilities/{id}/status
    @Transactional
    public FacilityAccount updateFacilityStatus(Long facilityId, FacilityStatus status) {
        FacilityAccount facility = getFacilityById(facilityId);
        facility.setStatus(status);
        auditLogService.log(null, "STATUS_CHANGE", "FacilityAccount", String.valueOf(facilityId));
        return facilityRepository.save(facility);
    }

    // GET /api/facilities/expiring
    @Transactional(readOnly = true)
    public List<FacilityAccount> getExpiringFacilities(int withinDays) {
        return facilityRepository.findExpiringFacilities(LocalDate.now(), LocalDate.now().plusDays(withinDays));
    }

    // POST /api/facilities/{facilityId}/renew
    @Transactional
    public LoanApplication renewFacility(Long facilityId) {
        FacilityAccount facility = getFacilityById(facilityId);
        if (facility.getStatus() != FacilityStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE facilities can be renewed");
        }
        LoanApplication renewal = LoanApplication.builder()
                .business(facility.getBusiness())
                .productType(facility.getProductType())
                .requestedAmount(facility.getSanctionedLimit())
                .status(ApplicationStatus.DRAFT)
                .applicationDate(LocalDate.now())
                .renewedFromFacilityId(facilityId)
                .build();
        LoanApplication saved = applicationRepository.save(renewal);
        auditLogService.log(null, "CREATE", "LoanApplication", String.valueOf(saved.getApplicationId()));
        log.info("Renewal application {} created for facility {}", saved.getApplicationId(), facilityId);
        return saved;
    }

    // GET /api/facilities/{facilityId}/renewal-history
    @Transactional(readOnly = true)
    public List<LoanApplication> getRenewalHistory(Long facilityId) {
        return applicationRepository.findByRenewedFromFacilityId(facilityId);
    }

    // ── Drawdown ──────────────────────────────────────────────────

    @Transactional
    public Drawdown requestDrawdown(Long facilityId, Drawdown drawdown) {
        FacilityAccount facility = getFacilityById(facilityId);
        String errorMessage = switch (facility.getStatus()) {
            case ACTIVE -> null;
            case EXPIRED -> "Facility has expired";
            case CLOSED -> "Facility is closed";
            case NPA -> "Facility is classified as NPA";
        };
        if (errorMessage != null) throw new BadRequestException(errorMessage);
        BigDecimal available = facility.getSanctionedLimit().subtract(facility.getDisbursedAmount());
        if (drawdown.getAmount().compareTo(available) > 0) {
            throw new BadRequestException("Drawdown amount exceeds available limit of " + available);
        }
        drawdown.setFacility(facility);
        drawdown.setStatus(DrawdownStatus.REQUESTED);
        Drawdown saved = drawdownRepository.save(drawdown);
        auditLogService.log(null, "CREATE", "Drawdown", String.valueOf(saved.getDrawdownId()));
        log.info("Drawdown requested for facility {}: amount={}", facilityId, drawdown.getAmount());
        return saved;
    }

    // PATCH /drawdowns/{id}/approve — Requested → Approved
    @Transactional
    public Drawdown approveDrawdown(Long drawdownId) {
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));
        if (drawdown.getStatus() != DrawdownStatus.REQUESTED) {
            throw new BadRequestException("Only REQUESTED drawdowns can be approved");
        }
        drawdown.setStatus(DrawdownStatus.APPROVED);
        auditLogService.log(null, "APPROVE", "Drawdown", String.valueOf(drawdownId));
        log.info("Drawdown {} approved", drawdownId);
        return drawdownRepository.save(drawdown);
    }

    // PATCH /drawdowns/{id}/disburse — Approved → Disbursed
    @Transactional
    public Drawdown disburseDrawdown(Long drawdownId) {
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));
        if (drawdown.getStatus() != DrawdownStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED drawdowns can be disbursed");
        }
        drawdown.setStatus(DrawdownStatus.DISBURSED);
        drawdown.setDisbursedDate(LocalDate.now());
        FacilityAccount facility = drawdown.getFacility();
        facility.setDisbursedAmount(facility.getDisbursedAmount().add(drawdown.getAmount()));
        facility.setOutstandingBalance(facility.getOutstandingBalance().add(drawdown.getAmount()));
        facilityRepository.save(facility);
        auditLogService.log(null, "STATUS_CHANGE", "Drawdown", String.valueOf(drawdownId));
        log.info("Drawdown {} disbursed: amount={}", drawdownId, drawdown.getAmount());
        return drawdownRepository.save(drawdown);
    }

    @Transactional
    public Drawdown repayDrawdown(Long drawdownId) {
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));
        if (drawdown.getStatus() != DrawdownStatus.DISBURSED) {
            throw new BadRequestException("Only DISBURSED drawdowns can be repaid");
        }
        drawdown.setStatus(DrawdownStatus.REPAID);
        drawdown.setRepaymentDate(LocalDate.now());
        FacilityAccount facility = drawdown.getFacility();
        facility.setOutstandingBalance(facility.getOutstandingBalance().subtract(drawdown.getAmount()));
        facilityRepository.save(facility);
        auditLogService.log(null, "STATUS_CHANGE", "Drawdown", String.valueOf(drawdownId));
        log.info("Drawdown {} repaid: amount={}", drawdownId, drawdown.getAmount());
        return drawdownRepository.save(drawdown);
    }

    @Transactional
    public Drawdown markOverdue(Long drawdownId) {
        Drawdown drawdown = drawdownRepository.findById(drawdownId)
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found: " + drawdownId));
        if (drawdown.getStatus() != DrawdownStatus.DISBURSED) {
            throw new BadRequestException("Only DISBURSED drawdowns can be marked overdue");
        }
        drawdown.setStatus(DrawdownStatus.OVERDUE);
        auditLogService.log(null, "STATUS_CHANGE", "Drawdown", String.valueOf(drawdownId));
        log.warn("Drawdown {} marked as OVERDUE", drawdownId);
        return drawdownRepository.save(drawdown);
    }

    @Transactional(readOnly = true)
    public List<Drawdown> getDrawdownsByFacility(Long facilityId) {
        return drawdownRepository.findByFacility_FacilityId(facilityId);
    }

    // ── Working Capital Utilisation ───────────────────────────────

    @Transactional
    public WorkingCapitalUtilisation recordUtilisation(Long facilityId, WorkingCapitalUtilisation utilisation) {
        FacilityAccount facility = getFacilityById(facilityId);
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

    // PUT /api/facilities/{facilityId}/utilisation/{id}
    @Transactional
    public WorkingCapitalUtilisation updateUtilisation(Long utilisationId, WorkingCapitalUtilisation updates) {
        WorkingCapitalUtilisation existing = utilisationRepository.findById(utilisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisation record not found: " + utilisationId));
        if (updates.getDrawingPower() != null) existing.setDrawingPower(updates.getDrawingPower());
        if (updates.getCurrentUtilisation() != null) existing.setCurrentUtilisation(updates.getCurrentUtilisation());
        // Recompute derived fields
        if (existing.getDrawingPower() != null && existing.getCurrentUtilisation() != null) {
            existing.setAvailableLimit(existing.getDrawingPower().subtract(existing.getCurrentUtilisation()));
            if (existing.getDrawingPower().compareTo(BigDecimal.ZERO) != 0) {
                existing.setUtilisationPercent(
                        existing.getCurrentUtilisation()
                                .divide(existing.getDrawingPower(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")));
            }
        }
        return utilisationRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<WorkingCapitalUtilisation> getUtilisationByFacility(Long facilityId) {
        return utilisationRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional(readOnly = true)
    public WorkingCapitalUtilisation getUtilisationById(Long utilisationId) {
        return utilisationRepository.findById(utilisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisation record not found: " + utilisationId));
    }

    // UPDATE collateral details (Registered status only)
    @Transactional
    public CollateralRecord updateCollateral(Long collateralId, CollateralRecord updates) {
        CollateralRecord existing = collateralRepository.findById(collateralId)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral not found: " + collateralId));
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getOwnerName() != null) existing.setOwnerName(updates.getOwnerName());
        if (updates.getMarketValue() != null) {
            existing.setMarketValue(updates.getMarketValue());
            // Recompute realisable value
            if (existing.getForceValuePercent() != null) {
                existing.setRealisableValue(
                        existing.getMarketValue()
                                .multiply(existing.getForceValuePercent())
                                .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP));
            }
        }
        if (updates.getForceValuePercent() != null) {
            existing.setForceValuePercent(updates.getForceValuePercent());
            if (existing.getMarketValue() != null) {
                existing.setRealisableValue(
                        existing.getMarketValue()
                                .multiply(existing.getForceValuePercent())
                                .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP));
            }
        }
        return collateralRepository.save(existing);
    }

    // GET collateral coverage ratio = sum(realisableValue) / sanctionedLimit
    @Transactional(readOnly = true)
    public java.math.BigDecimal getCollateralCoverageRatio(Long applicationId) {
        List<CollateralRecord> collaterals = collateralRepository.findByApplication_ApplicationId(applicationId);
        java.math.BigDecimal totalRealisable = collaterals.stream()
                .filter(c -> c.getRealisableValue() != null)
                .map(CollateralRecord::getRealisableValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return totalRealisable;
    }
}
