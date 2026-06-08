package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// CollateralRevaluation - periodic revaluation of a collateral asset
// Banks revalue collateral annually or when market conditions change
@Entity
@Table(name = "collateral_revaluation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralRevaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long revaluationId;

    // Revaluation belongs to one collateral record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collateral_id", nullable = false)
    private CollateralRecord collateral;

    private LocalDate revaluationDate;

    private BigDecimal previousValue;
    private BigDecimal newValue;

    private Long revaluedById;

    private BigDecimal changePercent;   // % change from previous value

    // Completed = done, PendingApproval = needs manager sign-off
    @Builder.Default
    private String status = "Completed";
}
