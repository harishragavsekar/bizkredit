package com.bizkredit.entity;

import com.bizkredit.enums.CovenantTemplateStatus;
import com.bizkredit.enums.CovenantType;
import com.bizkredit.enums.MonitoringFrequency;
import com.bizkredit.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "covenant_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CovenantTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Template name is required")
    private String templateName;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Covenant type is required")
    private CovenantType covenantType;

    @NotBlank(message = "Description is required")
    private String description;

    private BigDecimal defaultThresholdValue;

    @Enumerated(EnumType.STRING)
    private MonitoringFrequency defaultMonitoringFrequency;

    @ElementCollection(targetClass = ProductType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "covenant_template_product_types", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "product_type")
    @Builder.Default
    private List<ProductType> applicableProductTypes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CovenantTemplateStatus status = CovenantTemplateStatus.ACTIVE;

    private Long createdById;
}
