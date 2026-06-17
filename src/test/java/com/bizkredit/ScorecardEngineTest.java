package com.bizkredit;

import com.bizkredit.entity.FinancialStatement;
import com.bizkredit.entity.Promoter;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.entity.ScorecardModel;
import com.bizkredit.enums.EntityType;
import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.RiskCategory;
import com.bizkredit.enums.ScorecardStatus;
import com.bizkredit.service.ScorecardEngine;
import com.bizkredit.service.ScoringFieldResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScorecardEngineTest {

    private final ScorecardEngine engine = new ScorecardEngine();

    private ScorecardModel.ScorecardParameter numericParam(String name, String source, String field,
                                                            int weight, String bandsJson) {
        return new ScorecardModel.ScorecardParameter(name, source, field, new BigDecimal(weight), bandsJson);
    }

    private ScorecardModel.RatingBand band(int min, int max, String rating, RiskCategory risk) {
        return new ScorecardModel.RatingBand(min, max, rating, risk);
    }

    @Test
    void numericBand_picksCorrectBandAndWeightsContribution() {
        // debtEquityRatio: <1.0 -> 100pts, 1.0-2.0 -> 60pts, >=2.0 -> 20pts. Weight 100 (only parameter).
        String rules = "{\"bands\":["
                + "{\"min\":null,\"max\":1.0,\"points\":100},"
                + "{\"min\":1.0,\"max\":2.0,\"points\":60},"
                + "{\"min\":2.0,\"max\":null,\"points\":20}]}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .scorecardId(1L)
                .productType(ProductType.TERM_LOAN)
                .maxScore(100)
                .status(ScorecardStatus.ACTIVE)
                .parameters(List.of(numericParam("Debt/Equity", "FinancialStatement", "debtEquityRatio", 100, rules)))
                .ratingBands(List.of(
                        band(0, 50, "B", RiskCategory.HIGH),
                        band(51, 80, "BBB", RiskCategory.MEDIUM),
                        band(81, 100, "AAA", RiskCategory.LOW)))
                .build();

        FinancialStatement statement = FinancialStatement.builder()
                .debtEquityRatio(new BigDecimal("0.8")) // falls in <1.0 band -> 100 pts
                .build();

        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(
                statement, null, List.of());

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        assertThat(result.computedScore()).isEqualTo(100);
        assertThat(result.rating()).isEqualTo("AAA");
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(result.partialData()).isFalse();
    }

    @Test
    void categoricalRule_matchesByValueCaseInsensitive() {
        String rules = "{\"values\":{\"Manufacturing\":90,\"Trading\":60},\"default\":40}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .maxScore(100)
                .parameters(List.of(numericParam("Industry", "SMEBusiness", "industry", 100, rules)))
                .ratingBands(List.of(band(0, 100, "X", RiskCategory.MEDIUM)))
                .build();

        SMEBusiness business = SMEBusiness.builder().industry("manufacturing").build(); // different case
        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(null, business, List.of());

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        assertThat(result.computedScore()).isEqualTo(90); // matched "Manufacturing" ignoring case
    }

    @Test
    void categoricalRule_fallsBackToDefaultWhenValueNotListed() {
        String rules = "{\"values\":{\"Manufacturing\":90},\"default\":40}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .maxScore(100)
                .parameters(List.of(numericParam("Industry", "SMEBusiness", "industry", 100, rules)))
                .ratingBands(List.of(band(0, 100, "X", RiskCategory.MEDIUM)))
                .build();

        SMEBusiness business = SMEBusiness.builder().industry("Retail").build();
        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(null, business, List.of());

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        assertThat(result.computedScore()).isEqualTo(40);
    }

    @Test
    void missingData_parameterContributesZeroAndFlagsPartialData() {
        String rules = "{\"bands\":[{\"min\":0,\"max\":null,\"points\":100}]}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .maxScore(100)
                .parameters(List.of(
                        numericParam("Revenue", "FinancialStatement", "revenue", 50, rules),
                        numericParam("DSCR", "FinancialStatement", "dscr", 50, rules)))
                .ratingBands(List.of(band(0, 100, "X", RiskCategory.MEDIUM)))
                .build();

        // No financial statement at all -> both params unresolved
        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(null, null, List.of());

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        assertThat(result.computedScore()).isEqualTo(0);
        assertThat(result.partialData()).isTrue();
        assertThat(result.breakdown()).hasSize(2);
        assertThat(result.breakdown().get(0).scored()).isFalse();
    }

    @Test
    void promoterField_usesHighestShareholdingPromoter() {
        String rules = "{\"bands\":[{\"min\":700,\"max\":null,\"points\":100},{\"min\":0,\"max\":700,\"points\":30}]}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .maxScore(100)
                .parameters(List.of(numericParam("Promoter credit score", "Promoter", "creditScore", 100, rules)))
                .ratingBands(List.of(band(0, 100, "X", RiskCategory.MEDIUM)))
                .build();

        Promoter minor = Promoter.builder().creditScore(600).shareholdingPercent(new BigDecimal("10")).build();
        Promoter majority = Promoter.builder().creditScore(780).shareholdingPercent(new BigDecimal("90")).build();

        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(
                null, null, List.of(minor, majority));

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        // Should use majority promoter's 780 score -> 100 pts band
        assertThat(result.computedScore()).isEqualTo(100);
    }

    @Test
    void multipleParameters_weightedAndScaledToMaxScore() {
        String highIsGood = "{\"bands\":[{\"min\":0,\"max\":1,\"points\":20},{\"min\":1,\"max\":null,\"points\":100}]}";
        String entityRules = "{\"values\":{\"PRIVATE_LIMITED\":100,\"PROPRIETORSHIP\":50}}";

        ScorecardModel scorecard = ScorecardModel.builder()
                .maxScore(50) // non-100 max score, to verify scaling
                .parameters(List.of(
                        numericParam("Current ratio", "FinancialStatement", "currentRatio", 70, highIsGood),
                        numericParam("Entity type", "SMEBusiness", "entityType", 30, entityRules)))
                .ratingBands(List.of(
                        band(0, 30, "B", RiskCategory.HIGH),
                        band(31, 50, "AA", RiskCategory.LOW)))
                .build();

        FinancialStatement statement = FinancialStatement.builder()
                .currentRatio(new BigDecimal("1.5")) // -> 100 pts, weight 70
                .build();
        SMEBusiness business = SMEBusiness.builder().entityType(EntityType.PRIVATE_LIMITED).build(); // -> 100 pts, weight 30

        ScoringFieldResolver.ScoringContext context = new ScoringFieldResolver.ScoringContext(
                statement, business, List.of());

        ScorecardEngine.ScoringResult result = engine.compute(scorecard, context);

        // weighted = 100*0.70 + 100*0.30 = 100 (of 100) -> scaled to maxScore 50 -> 50
        assertThat(result.computedScore()).isEqualTo(50);
        assertThat(result.rating()).isEqualTo("AA");
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.LOW);
    }
}
