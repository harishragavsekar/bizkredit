package com.bizkredit.module5.entity;

import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.common.enums.CovenantStatus;
import com.bizkredit.common.enums.CovenantType;
import com.bizkredit.common.enums.MonitoringFrequency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "covenant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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