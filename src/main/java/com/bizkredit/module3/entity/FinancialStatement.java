package com.bizkredit.module3.entity;

import com.bizkredit.module2.entity.LoanApplication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "financial_statement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "application")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @NotBlank(message = "Financial year is required")
    private String financialYear;

    @PositiveOrZero(message = "Revenue cannot be negative")
    private BigDecimal revenue;

    private BigDecimal ebitda;

    private BigDecimal pat;

    @PositiveOrZero(message = "Total assets cannot be negative")
    private BigDecimal totalAssets;

    @PositiveOrZero(message = "Total liabilities cannot be negative")
    private BigDecimal totalLiabilities;

    private BigDecimal netWorth;

    private BigDecimal currentRatio;

    private BigDecimal debtEquityRatio;

    private BigDecimal dscr;

    private Long enteredById;

    @Builder.Default
    private String status = "Draft";
}