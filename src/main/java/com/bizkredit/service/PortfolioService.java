package com.bizkredit.service;

import com.bizkredit.repository.FacilityAccountRepository;
import com.bizkredit.repository.NPARecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final FacilityAccountRepository facilityRepository;
    private final NPARecordRepository npaRecordRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getPortfolioSummary() {
        Object[] raw = facilityRepository.getPortfolioSummary();
        Map<String, Object> result = new HashMap<>();

        if (raw == null || raw.length < 3) {
            result.put("totalSanctionedExposure", BigDecimal.ZERO);
            result.put("totalOutstanding", BigDecimal.ZERO);
            result.put("activeFacilitiesCount", 0);
            result.put("portfolioUtilisationPercent", 0);
            return result;
        }

        BigDecimal sanctioned = raw[0] != null ? (BigDecimal) raw[0] : BigDecimal.ZERO;
        BigDecimal outstanding = raw[1] != null ? (BigDecimal) raw[1] : BigDecimal.ZERO;
        long count = raw[2] != null ? ((Number) raw[2]).longValue() : 0;

        double utilisationPercent = sanctioned.compareTo(BigDecimal.ZERO) > 0
                ? outstanding.divide(sanctioned, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0;

        result.put("totalSanctionedExposure", sanctioned);
        result.put("totalOutstanding", outstanding);
        result.put("activeFacilitiesCount", count);
        result.put("portfolioUtilisationPercent", utilisationPercent);

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

        result.put("totalNPARecords", npaRecordRepository.findAll().size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSectorExposure() {
        return facilityRepository.getSectorExposure()
                .stream()
                .map(row -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("industry", row[0] != null ? row[0].toString() : "Unknown");
                    data.put("totalOutstanding", row[1] != null ? row[1] : BigDecimal.ZERO);
                    return data;
                })
                .collect(Collectors.toList());
    }
}