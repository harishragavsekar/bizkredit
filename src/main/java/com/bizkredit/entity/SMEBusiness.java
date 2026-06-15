package com.bizkredit.entity;

import com.bizkredit.enums.EntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SMEBusiness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Registration number is required")
    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    private String industry;

    @PositiveOrZero(message = "Years in operation cannot be negative")
    private Integer yearsInOperation;

    @PositiveOrZero(message = "Annual turnover cannot be negative")
    private BigDecimal annualTurnover;

    @PositiveOrZero(message = "Employee count cannot be negative")
    private Integer employeeCount;

    private String primaryBankId;

    @Builder.Default
    private String kycStatus = "Pending";

    @Builder.Default
    private String status = "Active";
}