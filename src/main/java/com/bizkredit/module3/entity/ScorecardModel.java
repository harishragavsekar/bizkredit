package com.bizkredit.module3.entity;

import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.RiskCategory;
import com.bizkredit.enums.ScorecardStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scorecard_model")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ScorecardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scorecardId;

    private String scorecardName;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScorecardStatus status = ScorecardStatus.DRAFT;

    private Long createdById;

    private LocalDate effectiveDate;

    @ElementCollection
    @CollectionTable(name = "scorecard_parameter", joinColumns = @JoinColumn(name = "scorecard_id"))
    @Builder.Default
    private List<ScorecardParameter> parameters = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "scorecard_rating_band", joinColumns = @JoinColumn(name = "scorecard_id"))
    @Builder.Default
    private List<RatingBand> ratingBands = new ArrayList<>();

    // ── Embeddable sub-entities

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScorecardParameter {
        private String parameterName;
        // FinancialStatement / SMEBusiness / Promoter
        private String fieldSource;
        private String fieldName;
        private BigDecimal weight;
        // JSON string for configurable scoring rules
        @Column(columnDefinition = "TEXT")
        private String scoringRules;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingBand {
        private Integer minScore;
        private Integer maxScore;
        private String rating;  // AAA, AA, A, BBB, BB, B etc.
        @Enumerated(EnumType.STRING)
        private RiskCategory riskCategory;
    }
}
