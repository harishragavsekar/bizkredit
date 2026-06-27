package com.bizkredit.module5.service;

import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.module4.repository.FacilityAccountRepository;
import com.bizkredit.module4.repository.DrawdownRepository;
import com.bizkredit.module5.entity.EarlyWarningSignal;
import com.bizkredit.module5.entity.NPARecord;
import com.bizkredit.module5.repository.EarlyWarningSignalRepository;
import com.bizkredit.module5.repository.NPARecordRepository;
import com.bizkredit.enums.*;
import com.bizkredit.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NPAClassificationService {

    private final FacilityAccountRepository facilityRepository;
    private final DrawdownRepository drawdownRepository;
    private final NPARecordRepository npaRecordRepository;
    private final EarlyWarningSignalRepository ewsRepository;

    @Transactional
    public int runClassification() {
        List<FacilityAccount> facilities = facilityRepository.findByStatus(FacilityStatus.ACTIVE);
        int classified = 0;

        for (FacilityAccount facility : facilities) {
            var overdueDrawdown = drawdownRepository.findByFacility_FacilityId(facility.getFacilityId())
                    .stream()
                    .filter(d -> d.getStatus() == DrawdownStatus.OVERDUE && d.getRepaymentDate() != null)
                    .findFirst();

            if (overdueDrawdown.isEmpty()) {
                continue;
            }

            long overdueDays = ChronoUnit.DAYS.between(
                    overdueDrawdown.get().getRepaymentDate(),
                    LocalDate.now()
            );

            if (overdueDays <= 0) {
                continue;
            }

            if (overdueDays <= 30) {
                createSMASignal(facility, EWSSeverity.GREEN);
            } else if (overdueDays <= 60) {
                createSMASignal(facility, EWSSeverity.AMBER);
            } else if (overdueDays <= 90) {
                createSMASignal(facility, EWSSeverity.RED);
            } else {
                classifyAsNPA(facility, (int) overdueDays);
                classified++;
            }
        }

        return classified;
    }

    @Transactional(readOnly = true)
    public List<NPARecord> getAllNPA(
            NPAProvisioningCategory category,
            NPARecordStatus status) {

        if (category != null && status != null) {
            return npaRecordRepository.findByProvisioningCategoryAndStatus(category, status);
        }

        if (status != null) {
            return npaRecordRepository.findByStatus(status);
        }

        return npaRecordRepository.findAll();
    }

    @Transactional
    public NPARecord upgradeNPA(Long npaId) {
        NPARecord record = npaRecordRepository.findById(npaId)
                .orElseThrow(() -> new ResourceNotFoundException("NPA record not found"));

        record.setStatus(NPARecordStatus.UPGRADED);

        FacilityAccount facility = record.getFacility();
        facility.setStatus(FacilityStatus.ACTIVE);
        facilityRepository.save(facility);

        return npaRecordRepository.save(record);
    }

    private void createSMASignal(FacilityAccount facility, EWSSeverity severity) {
        boolean exists = ewsRepository.findByFacility_FacilityId(facility.getFacilityId())
                .stream()
                .anyMatch(e -> e.getSeverity() == severity && e.getStatus() == EWSStatus.OPEN);

        if (exists) {
            return;
        }

        ewsRepository.save(EarlyWarningSignal.builder()
                .facility(facility)
                .signalType(EWSSignalType.DOWNGRADE)
                .severity(severity)
                .detectedDate(LocalDate.now())
                .status(EWSStatus.OPEN)
                .build());
    }

    private void classifyAsNPA(FacilityAccount facility, int overdueDays) {
        boolean exists = npaRecordRepository
                .findByFacility_FacilityIdAndStatus(
                        facility.getFacilityId(),
                        NPARecordStatus.ACTIVE
                )
                .isPresent();

        if (exists) {
            return;
        }

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
    }
}