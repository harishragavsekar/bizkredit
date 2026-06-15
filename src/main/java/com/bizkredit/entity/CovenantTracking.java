package com.bizkredit.entity;

import com.bizkredit.enums.ComplianceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "covenant_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CovenantTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "covenant_id", nullable = false)
    private Covenant covenant;

    private String period;

    private BigDecimal actualValue;

    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComplianceStatus complianceStatus = ComplianceStatus.DATA_AWAITED;

    private Long reviewedById;

    private LocalDate reviewDate;
}
