package com.bizkredit.entity;

import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "business")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private SMEBusiness business;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Requested amount is required")
    @Positive(message = "Requested amount must be positive")
    private BigDecimal requestedAmount;

    @Positive(message = "Tenure must be positive")
    private Integer tenure;

    private String purpose;

    @Builder.Default
    private LocalDate applicationDate = LocalDate.now();

    private Long assignedAnalystId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;
}