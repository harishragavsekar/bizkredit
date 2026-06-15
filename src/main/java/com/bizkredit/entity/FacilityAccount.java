package com.bizkredit.entity;

import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "facility_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"application", "business"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FacilityAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private SMEBusiness business;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Sanctioned limit is required")
    @Positive(message = "Sanctioned limit must be positive")
    private BigDecimal sanctionedLimit;

    private BigDecimal disbursedAmount;

    private BigDecimal outstandingBalance;

    @Positive(message = "Interest rate must be positive")
    private BigDecimal interestRate;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FacilityStatus status = FacilityStatus.ACTIVE;
}