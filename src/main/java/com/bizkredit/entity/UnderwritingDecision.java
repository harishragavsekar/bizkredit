package com.bizkredit.entity;

import com.bizkredit.enums.DecisionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "underwriting_decision")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "proposal")
public class UnderwritingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private CreditProposal proposal;

    private Long managerId;
    private BigDecimal sanctionedAmount;
    private BigDecimal approvedRate;
    private Integer tenure;

    @Column(length = 1000)
    private String specialConditions;

    @Builder.Default
    private LocalDate decisionDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private DecisionStatus status;
}
