package com.bizkredit.entity;

import com.bizkredit.enums.DrawdownStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Drawdown - individual disbursement request against a facility
// A facility can have multiple drawdowns up to the sanctioned limit
@Entity
@Table(name = "drawdown")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drawdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawdownId;

    // Drawdown belongs to one facility account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    private BigDecimal amount;
    private String purpose;

    @Builder.Default
    private LocalDate requestDate = LocalDate.now();

    private LocalDate disbursedDate;
    private LocalDate repaymentDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DrawdownStatus status = DrawdownStatus.REQUESTED;
}
