package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// WorkingCapitalUtilisation - tracks monthly utilisation of working capital facility
// Drawing power = max amount that can be drawn based on current stock/debtors
@Entity
@Table(name = "working_capital_utilisation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalUtilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long utilisationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    private String period;                  // e.g. "Jun-2026"

    private BigDecimal drawingPower;        // Max drawable based on stock/debtors
    private BigDecimal currentUtilisation;  // Amount currently drawn
    private BigDecimal availableLimit;      // drawingPower - currentUtilisation

    private BigDecimal utilisationPercent;  // currentUtilisation / drawingPower * 100

    @Builder.Default
    private LocalDate recordedDate = LocalDate.now();
}
