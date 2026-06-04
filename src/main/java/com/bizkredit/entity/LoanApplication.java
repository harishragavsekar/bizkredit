package com.bizkredit.entity;

import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// LoanApplication - submitted by an SME business for a specific product
// Linked to a business, goes through a status pipeline until disbursed
@Entity
@Table(name = "loan_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    // Application belongs to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private SMEBusiness business;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ProductType productType;

    @Positive
    private BigDecimal requestedAmount;

    private Integer tenure;         // Loan tenure in months
    private String purpose;         // Purpose of the loan

    @Builder.Default
    private LocalDate applicationDate = LocalDate.now();

    private Long assignedAnalystId; // Credit analyst assigned to review

    // Tracks where the application is in the pipeline
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;
}
