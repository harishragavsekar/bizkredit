package com.bizkredit.entity;

import com.bizkredit.enums.AssetType;
import com.bizkredit.enums.CollateralStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// CollateralRecord - asset registered as security against a loan application
// Coverage ratio = realisable value / sanctioned amount (should be >= 1)
@Entity
@Table(name = "collateral_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collateralId;

    // Collateral is registered against a loan application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    private String description;
    private String ownerName;

    private BigDecimal marketValue;         // Current market value

    private BigDecimal forceValuePercent;   // % of market value bank can realise

    // Realisable value = marketValue * forceValuePercent / 100
    private BigDecimal realisableValue;

    private LocalDate valuationDate;

    private Long valuedById;                // Collateral evaluator who assessed

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CollateralStatus status = CollateralStatus.REGISTERED;
}
