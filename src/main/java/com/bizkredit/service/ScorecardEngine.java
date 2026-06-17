package com.bizkredit.service;

import com.bizkredit.entity.ScorecardModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a ScorecardModel against a real application's data (financial statement,
 * SME business, promoters) and produces a weighted score, a rating band match, and a
 * per-parameter breakdown for transparency/audit.
 *
 * Replaces the Phase-1 stub that returned a fixed maxScore/2, "B", "MEDIUM" regardless
 * of input.
 */
@Slf4j
@Component
public class ScorecardEngine {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScoringResult compute(ScorecardModel scorecard, ScoringFieldResolver.ScoringContext context) {
        List<ParameterResult> breakdown = new ArrayList<>();
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeightApplied = BigDecimal.ZERO;

        for (ScorecardModel.ScorecardParameter param : scorecard.getParameters()) {
            ParameterResult result = scoreParameter(param, context);
            breakdown.add(result);
            if (result.scored()) {
                BigDecimal weight = param.getWeight() != null ? param.getWeight() : BigDecimal.ZERO;
                // points are 0-100; weight is a percentage that sums to 100 across the scorecard
                BigDecimal contribution = result.points()
                        .multiply(weight)
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                totalWeightedScore = totalWeightedScore.add(contribution);
                totalWeightApplied = totalWeightApplied.add(weight);
            }
        }

        // totalWeightedScore is on a 0-100 scale (since weights sum to 100). Scale to the
        // scorecard's configured maxScore.
        int maxScore = scorecard.getMaxScore() != null ? scorecard.getMaxScore() : 100;
        BigDecimal scaledScore = totalWeightedScore
                .multiply(BigDecimal.valueOf(maxScore))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        ScorecardModel.RatingBand matchedBand = matchBand(scorecard.getRatingBands(), scaledScore);

        boolean partialData = totalWeightApplied.compareTo(new BigDecimal("100")) < 0
                && !scorecard.getParameters().isEmpty();

        return new ScoringResult(
                scaledScore.intValue(),
                maxScore,
                matchedBand != null ? matchedBand.getRating() : null,
                matchedBand != null ? matchedBand.getRiskCategory() : null,
                breakdown,
                partialData,
                totalWeightApplied
        );
    }

    private ParameterResult scoreParameter(ScorecardModel.ScorecardParameter param,
                                            ScoringFieldResolver.ScoringContext context) {
        Object rawValue = ScoringFieldResolver.resolve(param.getFieldSource(), param.getFieldName(), context);

        if (rawValue == null) {
            return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                    param.getFieldName(), null, BigDecimal.ZERO, false,
                    "No data available for this field");
        }

        ScoringRule rule;
        try {
            rule = objectMapper.readValue(param.getScoringRules(), ScoringRule.class);
        } catch (Exception e) {
            log.warn("Could not parse scoringRules for parameter '{}': {}", param.getParameterName(), e.getMessage());
            return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                    param.getFieldName(), rawValue, BigDecimal.ZERO, false,
                    "Invalid scoringRules configuration");
        }

        if (rule.getBands() != null && !rule.getBands().isEmpty()) {
            return scoreNumericBands(param, rule, rawValue);
        }
        if (rule.getValues() != null && !rule.getValues().isEmpty()) {
            return scoreCategorical(param, rule, rawValue);
        }

        return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                param.getFieldName(), rawValue, BigDecimal.ZERO, false,
                "scoringRules has neither 'bands' nor 'values'");
    }

    private ParameterResult scoreNumericBands(ScorecardModel.ScorecardParameter param, ScoringRule rule, Object rawValue) {
        BigDecimal numeric = ScoringFieldResolver.asNumeric(rawValue);
        if (numeric == null) {
            return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                    param.getFieldName(), rawValue, BigDecimal.ZERO, false,
                    "Field value is not numeric");
        }
        for (ScoringRule.Band band : rule.getBands()) {
            boolean aboveMin = band.getMin() == null || numeric.compareTo(band.getMin()) >= 0;
            boolean belowMax = band.getMax() == null || numeric.compareTo(band.getMax()) < 0;
            if (aboveMin && belowMax) {
                BigDecimal points = band.getPoints() != null ? band.getPoints() : BigDecimal.ZERO;
                return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                        param.getFieldName(), numeric, points, true, null);
            }
        }
        return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                param.getFieldName(), numeric, BigDecimal.ZERO, false,
                "Value did not fall into any configured band");
    }

    private ParameterResult scoreCategorical(ScorecardModel.ScorecardParameter param, ScoringRule rule, Object rawValue) {
        String text = rawValue.toString();
        for (Map.Entry<String, BigDecimal> entry : rule.getValues().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(text)) {
                return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                        param.getFieldName(), rawValue, entry.getValue(), true, null);
            }
        }
        if (rule.getDefault() != null) {
            return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                    param.getFieldName(), rawValue, rule.getDefault(), true,
                    "Used default — value '" + text + "' not in configured list");
        }
        return new ParameterResult(param.getParameterName(), param.getFieldSource(),
                param.getFieldName(), rawValue, BigDecimal.ZERO, false,
                "Value '" + text + "' not in configured list and no default set");
    }

    private ScorecardModel.RatingBand matchBand(List<ScorecardModel.RatingBand> bands, BigDecimal score) {
        if (bands == null) return null;
        for (ScorecardModel.RatingBand band : bands) {
            boolean aboveMin = band.getMinScore() == null || score.compareTo(BigDecimal.valueOf(band.getMinScore())) >= 0;
            boolean belowMax = band.getMaxScore() == null || score.compareTo(BigDecimal.valueOf(band.getMaxScore())) <= 0;
            if (aboveMin && belowMax) return band;
        }
        return null;
    }

    /** One row of the per-parameter score breakdown, returned for transparency/audit. */
    public record ParameterResult(
            String parameterName,
            String fieldSource,
            String fieldName,
            Object rawValue,
            BigDecimal points,
            boolean scored,
            String note
    ) {
    }

    /** Final outcome of a scorecard computation. */
    public record ScoringResult(
            int computedScore,
            int maxScore,
            String rating,
            com.bizkredit.enums.RiskCategory riskCategory,
            List<ParameterResult> breakdown,
            boolean partialData,
            BigDecimal totalWeightApplied
    ) {
    }
}
