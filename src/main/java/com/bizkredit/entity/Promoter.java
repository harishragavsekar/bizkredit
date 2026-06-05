package com.bizkredit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Promoter entity - directors or owners of the SME business
// Multiple promoters can be linked to one business
@Entity
@Table(name = "promoter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promoter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promoterId;

    // Many promoters belong to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private SMEBusiness business;

    @NotBlank
    private String name;

    private String nationalIdRef;           // Aadhaar/PAN reference
    private BigDecimal shareholdingPercent; // Ownership percentage
    private BigDecimal personalNetWorth;
    private Integer creditScore;

    @Builder.Default
    private String status = "Active";
}
