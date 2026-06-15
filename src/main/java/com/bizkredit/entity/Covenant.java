package com.bizkredit.entity;

import java.math.BigDecimal;

import com.bizkredit.enums.CovenantStatus;
import com.bizkredit.enums.CovenantType;
import com.bizkredit.enums.MonitoringFrequency;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "covenant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Covenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long covenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Covenant type is required")
    private CovenantType covenantType;

    @NotBlank(message = "Description is required")
    private String description;

    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    private MonitoringFrequency monitoringFrequency;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CovenantStatus status = CovenantStatus.ACTIVE;
}
