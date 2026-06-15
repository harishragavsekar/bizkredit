package com.bizkredit.entity;

import com.bizkredit.enums.AnalystRecommendation;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.enums.RiskCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_proposal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "application")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CreditProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    private Long analystId;

    @Positive(message = "Scorecard rating must be positive")
    private BigDecimal scorecardRating;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;

    @Positive(message = "Suggested amount must be positive")
    private BigDecimal suggestedAmount;

    @Positive(message = "Suggested rate must be positive")
    private BigDecimal suggestedRate;

    private Integer tenure;

    @Column(length = 1000)
    private String conditions;

    @Enumerated(EnumType.STRING)
    private AnalystRecommendation analystRecommendation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.DRAFT;
}