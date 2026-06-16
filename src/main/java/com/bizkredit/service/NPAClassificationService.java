package com.bizkredit.service;

import com.bizkredit.entity.EarlyWarningSignal;
import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.entity.NPARecord;
import com.bizkredit.enums.*;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NPAClassificationService {

    private final FacilityAccountRepository facilityRepository;
    private final DrawdownRepository drawdownRepository;
    private final NPARecordRepository npaRecordRepository;
    private final EarlyWarningSignalRepository ewsRepository;
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;

    // POST /api/npa/classify — run classification engine
    @Transactional
    public int runClassification() {
        List<FacilityAccount> activeFacilities = facilityRepository.findByStatus(FacilityStatus.ACTIVE);
        int classified = 0;

        for (FacilityAccount facility : activeFacilities) {
            // Find most overdue drawdown
            var overdueDraw = drawdownRepository.findByFacility_FacilityId(facility.getFacilityId())
                    .stream()
                    .filter(d -> d.getStatus() == DrawdownStatus.OVERDUE && d.getRepaymentDate() != null)
                    .max((a, b) -> {
                        long daysA = ChronoUnit.DAYS.between(a.getRepaymentDate(), LocalDate.now());
                        long daysB = ChronoUnit.DAYS.between(b.getRepaymentDate(), LocalDate.now());
                        return Long.compare(daysA, daysB);
                    });

            if (overdueDraw.isEmpty()) continue;

            long overdueDays = ChronoUnit.DAYS.between(
                    overdueDraw.get().getRepaymentDate(), LocalDate.now());

            if (overdueDays <= 0) continue;

            if (overdueDays <= 30) {
                // SMA-0 — Green EWS
                createSMASignal(facility, (int) overdueDays, EWSSeverity.GREEN);
            } else if (overdueDays <= 60) {
                // SMA-1 — Amber EWS
                createSMASignal(facility, (int) overdueDays, EWSSeverity.AMBER);
            } else if (overdueDays <= 90) {
                // SMA-2 — Red EWS + notify
                createSMASignal(facility, (int) overdueDays, EWSSeverity.RED);
            } else {
                // NPA — transition facility + create record
                classifyAsNPA(facility, (int) overdueDays);
                classified++;
            }
        }

        log.info("NPA classification run complete. {} facilities classified as NPA", classified);
        return classified;
    }

    private void createSMASignal(FacilityAccount facility, int overdueDays, EWSSeverity severity) {
        boolean exists = ewsRepository.findByFacility_FacilityId(facility.getFacilityId())
                .stream()
                .anyMatch(e -> e.getSignalType() == EWSSignalType.DOWNGRADE
                        && e.getSeverity() == severity
                        && e.getStatus() == EWSStatus.OPEN);
        if (!exists) {
            ewsRepository.save(EarlyWarningSignal.builder()
                    .facility(facility)
                    .signalType(EWSSignalType.DOWNGRADE)
                    .severity(severity)
                    .detectedDate(LocalDate.now())
                    .status(EWSStatus.OPEN)
                    .build());
            log.warn("SMA signal created for facility {}: {} days overdue, severity {}",
                    facility.getFacilityId(), overdueDays, severity);
        }
    }

    private void classifyAsNPA(FacilityAccount facility, int overdueDays) {
        // Only create new NPA record if one doesn't already exist
        boolean alreadyNPA = npaRecordRepository
                .findByFacility_FacilityIdAndStatus(facility.getFacilityId(), NPARecordStatus.ACTIVE)
                .isPresent();

        if (!alreadyNPA) {
            facility.setStatus(FacilityStatus.NPA);
            facilityRepository.save(facility);

            NPARecord record = NPARecord.builder()
                    .facility(facility)
                    .classificationDate(LocalDate.now())
                    .overdueDays(overdueDays)
                    .outstandingAtClassification(facility.getOutstandingBalance())
                    .provisioningCategory(NPAProvisioningCategory.SUB_STANDARD)
                    .status(NPARecordStatus.ACTIVE)
                    .build();
            npaRecordRepository.save(record);
            auditLogService.log(null, "STATUS_CHANGE", "FacilityAccount",
                    String.valueOf(facility.getFacilityId()));
            log.warn("Facility {} classified as NPA after {} days overdue",
                    facility.getFacilityId(), overdueDays);
        }
    }

    @Transactional(readOnly = true)
    public List<NPARecord> getAllNPA(NPAProvisioningCategory category, NPARecordStatus status) {
        if (category != null && status != null) {
            return npaRecordRepository.findByProvisioningCategoryAndStatus(category, status);
        }
        if (status != null) return npaRecordRepository.findByStatus(status);
        return npaRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<NPARecord> getHistoryByFacility(Long facilityId) {
        return npaRecordRepository.findByFacility_FacilityId(facilityId);
    }

    // PUT /api/npa/{npaId}/upgrade
    @Transactional
    public NPARecord upgradeNPA(Long npaId) {
        NPARecord record = npaRecordRepository.findById(npaId)
                .orElseThrow(() -> new ResourceNotFoundException("NPA record not found: " + npaId));
        record.setStatus(NPARecordStatus.UPGRADED);
        FacilityAccount facility = record.getFacility();
        facility.setStatus(FacilityStatus.ACTIVE);
        facilityRepository.save(facility);
        auditLogService.log(null, "STATUS_CHANGE", "FacilityAccount",
                String.valueOf(facility.getFacilityId()));
        log.info("Facility {} upgraded from NPA to Active", facility.getFacilityId());
        return npaRecordRepository.save(record);
    }
}
