package com.bizkredit.entity;

import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.ProductType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// FacilityAccount - created after underwriting approval
// Represents the actual loan account from which drawdowns are made
@Entity
@Table(name = "facility_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private ProductType productType;

    private BigDecimal sanctionedLimit;     // Total approved amount
    private BigDecimal disbursedAmount;     // Amount already disbursed
    private BigDecimal outstandingBalance;  // Amount yet to be repaid

    private BigDecimal interestRate;

    private LocalDate expiryDate;           // Facility validity end date

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FacilityStatus status = FacilityStatus.ACTIVE;
}
