package com.bizkredit.module4.entity;

import com.bizkredit.module2.entity.LoanApplication;
import com.bizkredit.common.enums.AssetType;
import com.bizkredit.common.enums.CollateralStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "collateral_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "application")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CollateralRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collateralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    private String description;

    private String ownerName;

    @NotNull(message = "Market value is required")
    @Positive(message = "Market value must be positive")
    private BigDecimal marketValue;

    @Positive(message = "Force value percent must be positive")
    private BigDecimal forceValuePercent;

    private BigDecimal realisableValue;

    private LocalDate valuationDate;

    private Long valuedById;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CollateralStatus status = CollateralStatus.REGISTERED;
}