package com.bizkredit.entity;

import com.bizkredit.enums.EWSSeverity;
import com.bizkredit.enums.EWSSignalType;
import com.bizkredit.enums.EWSStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "early_warning_signal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EarlyWarningSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ewsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    @Enumerated(EnumType.STRING)
    private EWSSignalType signalType;

    @Enumerated(EnumType.STRING)
    private EWSSeverity severity;

    @Builder.Default
    private LocalDate detectedDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EWSStatus status = EWSStatus.OPEN;
}