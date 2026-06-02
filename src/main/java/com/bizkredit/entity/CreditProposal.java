package com.bizkredit.entity;

import com.bizkredit.enums.AnalystRecommendation;
import com.bizkredit.enums.ProposalStatus;
import com.bizkredit.enums.RiskCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_proposal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    private Long analystId;

    private BigDecimal scorecardRating;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;

    private BigDecimal suggestedAmount;
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
