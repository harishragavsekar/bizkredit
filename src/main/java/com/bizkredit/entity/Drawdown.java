package com.bizkredit.entity;

import com.bizkredit.enums.DrawdownStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "drawdown")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "facility")
public class Drawdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawdownId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
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
