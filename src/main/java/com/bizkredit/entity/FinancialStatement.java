package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// FinancialStatement - multi-year financial data for a loan application
// Ratios are auto-computed by the service when raw data is entered
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

    // Each statement belongs to one loan application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    private String financialYear;       // e.g. "2023-24"

    // Raw financial figures entered by analyst
    private BigDecimal revenue;
    private BigDecimal ebitda;          // Earnings Before Interest, Tax, Depreciation
    private BigDecimal pat;             // Profit After Tax
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;

    // Auto-computed ratios - calculated in service layer
    private BigDecimal currentRatio;    // totalAssets / totalLiabilities
    private BigDecimal debtEquityRatio; // totalLiabilities / netWorth
    private BigDecimal dscr;            // ebitda / totalLiabilities (simplified)

    private Long enteredById;           // Analyst who entered the data

    // Draft = entered, Verified = reviewed and confirmed
    @Builder.Default
    private String status = "Draft";
}
