package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "working_capital_utilisation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "facility")
public class WorkingCapitalUtilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long utilisationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    private String period;
    private BigDecimal drawingPower;
    private BigDecimal currentUtilisation;
    private BigDecimal availableLimit;
    private BigDecimal utilisationPercent;

    @Builder.Default
    private LocalDate recordedDate = LocalDate.now();
}
