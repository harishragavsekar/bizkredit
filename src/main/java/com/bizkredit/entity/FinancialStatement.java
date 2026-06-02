package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "financial_statement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    private String financialYear;       // e.g. "2023-24"

    private BigDecimal revenue;
    private BigDecimal ebitda;
    private BigDecimal pat;             // Profit After Tax
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;

    // Computed ratios
    private BigDecimal currentRatio;
    private BigDecimal debtEquityRatio;
    private BigDecimal dscr;            // Debt Service Coverage Ratio

    private Long enteredById;

    @Builder.Default
    private String status = "Draft";    // Draft / Verified
}
