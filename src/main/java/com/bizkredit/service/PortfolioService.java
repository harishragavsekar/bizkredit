package com.bizkredit.service;

import com.bizkredit.enums.CovenantStatus;
import com.bizkredit.enums.EWSStatus;
import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final FacilityAccountRepository facilityRepository;
    private final CovenantRepository covenantRepository;
    private final CovenantTrackingRepository trackingRepository;
    private final EarlyWarningSignalRepository ewsRepository;
    private final NPARecordRepository npaRecordRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getPortfolioSummary() {
        Object[] raw = facilityRepository.getPortfolioSummary();
        Map<String, Object> result = new HashMap<>();
        if (raw != null && raw.length >= 3) {
            BigDecimal sanctioned = raw[0] != null ? (BigDecimal) raw[0] : BigDecimal.ZERO;
            BigDecimal outstanding = raw[1] != null ? (BigDecimal) raw[1] : BigDecimal.ZERO;
            long count = raw[2] != null ? ((Number) raw[2]).longValue() : 0;
            double utilPct = sanctioned.compareTo(BigDecimal.ZERO) > 0
                    ? outstanding.divide(sanctioned, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0;
            result.put("totalSanctionedExposure", sanctioned);
            result.put("totalOutstanding", outstanding);
            result.put("activeFacilitiesCount", count);
            result.put("portfolioUtilisationPercent", utilPct);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssetQuality() {
        List<Object[]> distribution = facilityRepository.getAssetQualityDistribution();
        Map<String, Object> result = new HashMap<>();
        for (Object[] row : distribution) {
            if (row[0] != null) {
                result.put(row[0].toString(), Map.of(
                        "count", row[1] != null ? ((Number) row[1]).longValue() : 0,
                        "outstanding", row[2] != null ? row[2] : BigDecimal.ZERO
                ));
            }
        }
        // Add NPA ratio
        long totalNPA = npaRecordRepository.findAll().size();
        result.put("totalNPARecords", totalNPA);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSectorExposure() {
        List<Object[]> raw = facilityRepository.getSectorExposure();
        return raw.stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("industry", row[0] != null ? row[0].toString() : "Unknown");
                    m.put("totalOutstanding", row[1] != null ? row[1] : BigDecimal.ZERO);
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCovenantCompliance() {
        long totalActive = covenantRepository.findByStatus(CovenantStatus.ACTIVE).size();
        long breached = covenantRepository.findByStatus(CovenantStatus.BREACHED).size();
        long compliant = totalActive - breached;
        double complianceRate = totalActive > 0 ? (compliant * 100.0 / totalActive) : 100.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalActiveCovenants", totalActive);
        result.put("compliantCount", compliant);
        result.put("breachedCount", breached);
        result.put("complianceRatePercent", complianceRate);
        result.put("breachedCovenants", covenantRepository.findByStatus(CovenantStatus.BREACHED));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEWSSignals() {
        var openSignals = ewsRepository.findByStatus(EWSStatus.OPEN);
        Map<String, Long> bySeverity = openSignals.stream()
                .collect(Collectors.groupingBy(e -> e.getSeverity().name(), Collectors.counting()));
        Map<String, Long> byType = openSignals.stream()
                .collect(Collectors.groupingBy(e -> e.getSignalType().name(), Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("totalOpenSignals", openSignals.size());
        result.put("bySeverity", bySeverity);
        result.put("bySignalType", byType);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRenewalPipeline() {
        Map<String, Object> result = new HashMap<>();
        result.put("expiring30Days", facilityRepository.findExpiringFacilities(LocalDate.now(), LocalDate.now().plusDays(30)));
        result.put("expiring60Days", facilityRepository.findExpiringFacilities(LocalDate.now().plusDays(31), LocalDate.now().plusDays(60)));
        result.put("expiring90Days", facilityRepository.findExpiringFacilities(LocalDate.now().plusDays(61), LocalDate.now().plusDays(90)));
        return result;
    }
}
