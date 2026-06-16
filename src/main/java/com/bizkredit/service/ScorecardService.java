package com.bizkredit.service;

import com.bizkredit.entity.ScorecardModel;
import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.ScorecardStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.ScorecardModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScorecardService {

    private final ScorecardModelRepository scorecardRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ScorecardModel createScorecard(ScorecardModel scorecard, Long createdById) {
        validateScorecard(scorecard);

        // Only one active scorecard allowed per product type
        if (scorecard.getStatus() == ScorecardStatus.ACTIVE) {
            List<ScorecardModel> existing = scorecardRepository.findByProductTypeAndStatus(
                    scorecard.getProductType(), ScorecardStatus.ACTIVE);
            if (!existing.isEmpty()) {
                throw new BadRequestException(
                        "An active scorecard already exists for " + scorecard.getProductType());
            }
        }

        scorecard.setCreatedById(createdById);
        ScorecardModel saved = scorecardRepository.save(scorecard);
        auditLogService.log(createdById, "CREATE", "ScorecardModel", String.valueOf(saved.getScorecardId()));
        log.info("Scorecard created: {} for {}", saved.getScorecardName(), saved.getProductType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ScorecardModel> getScorecards(ProductType productType, ScorecardStatus status) {
        if (productType != null && status != null) {
            return scorecardRepository.findByProductTypeAndStatus(productType, status);
        }
        if (status != null) return scorecardRepository.findByStatus(status);
        return scorecardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ScorecardModel getById(Long scorecardId) {
        return scorecardRepository.findById(scorecardId)
                .orElseThrow(() -> new ResourceNotFoundException("Scorecard not found: " + scorecardId));
    }

    @Transactional
    public ScorecardModel updateScorecard(Long scorecardId, ScorecardModel updates) {
        ScorecardModel existing = getById(scorecardId);
        if (existing.getStatus() == ScorecardStatus.ACTIVE) {
            throw new BadRequestException("Cannot update an ACTIVE scorecard. Archive it first.");
        }
        if (updates.getScorecardName() != null) existing.setScorecardName(updates.getScorecardName());
        if (updates.getMaxScore() != null) existing.setMaxScore(updates.getMaxScore());
        if (updates.getParameters() != null) existing.setParameters(updates.getParameters());
        if (updates.getRatingBands() != null) existing.setRatingBands(updates.getRatingBands());
        if (updates.getEffectiveDate() != null) existing.setEffectiveDate(updates.getEffectiveDate());
        validateScorecard(existing);
        auditLogService.log(null, "UPDATE", "ScorecardModel", String.valueOf(scorecardId));
        return scorecardRepository.save(existing);
    }

    // POST /api/scorecards/{id}/compute — stub, Phase 2 to implement full scoring engine
    @Transactional(readOnly = true)
    public Map<String, Object> computeScore(Long scorecardId, Long applicationId) {
        ScorecardModel scorecard = getById(scorecardId);
        Map<String, Object> result = new HashMap<>();
        result.put("scorecardId", scorecardId);
        result.put("applicationId", applicationId);
        result.put("computedScore", scorecard.getMaxScore() != null ? scorecard.getMaxScore() / 2 : 0);
        result.put("scorecardRating", "B");
        result.put("riskCategory", "MEDIUM");
        result.put("note", "Stub compute — full scoring engine in Phase 2");
        return result;
    }

    private void validateScorecard(ScorecardModel scorecard) {
        if (scorecard.getParameters() != null && !scorecard.getParameters().isEmpty()) {
            BigDecimal totalWeight = scorecard.getParameters().stream()
                    .map(p -> p.getWeight() != null ? p.getWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
                throw new BadRequestException(
                        "Parameter weights must sum to 100. Current sum: " + totalWeight);
            }
        }
    }
}
