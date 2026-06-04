package com.bizkredit.entity;

import com.bizkredit.enums.DecisionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// UnderwritingDecision - manager's final decision on a credit proposal
// This is the maker-checker step before facility creation
@Entity
@Table(name = "underwriting_decision")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderwritingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    // Decision is made on a specific credit proposal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private CreditProposal proposal;

    private Long managerId;

    private BigDecimal sanctionedAmount;    // Final approved loan amount
    private BigDecimal approvedRate;        // Final approved interest rate
    private Integer tenure;

    @Column(length = 1000)
    private String specialConditions;

    @Builder.Default
    private LocalDate decisionDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private DecisionStatus status;          // APPROVED / DECLINED / CONDITIONAL
}
