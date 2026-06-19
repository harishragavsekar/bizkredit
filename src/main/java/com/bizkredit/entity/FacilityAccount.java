package com.bizkredit.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JsonIgnore
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