package com.bizkredit.entity;

import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "npa_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "facility")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NPARecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long npaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityAccount facility;

    @Builder.Default
    private LocalDate classificationDate = LocalDate.now();

    private Integer overdueDays;

    private BigDecimal outstandingAtClassification;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NPAProvisioningCategory provisioningCategory = NPAProvisioningCategory.SUB_STANDARD;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NPARecordStatus status = NPARecordStatus.ACTIVE;
}
