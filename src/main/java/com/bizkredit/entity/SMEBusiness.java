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

    @Builder.Default
    private String kycStatus = "Pending";   // Pending / Verified / Rejected

    @Builder.Default
    private String status = "Active";       // Active / Inactive / Blacklisted
}
