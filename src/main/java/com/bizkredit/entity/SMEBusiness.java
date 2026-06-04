package com.bizkredit.entity;

import com.bizkredit.enums.EntityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// SMEBusiness entity - represents a business applying for a loan
// One business can have multiple loan applications
@Entity
@Table(name = "sme_business")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMEBusiness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    @NotBlank
    private String businessName;

    // Unique registration number (CIN/GSTIN/etc.)
    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EntityType entityType;

    private String industry;
    private Integer yearsInOperation;
    private BigDecimal annualTurnover;
    private Integer employeeCount;
    private String primaryBankId;

    // KYC status - Pending / Verified / Rejected
    @Builder.Default
    private String kycStatus = "Pending";

    // Business status - Active / Inactive / Blacklisted
    @Builder.Default
    private String status = "Active";
}
