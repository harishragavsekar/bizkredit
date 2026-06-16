package com.bizkredit.service;

import com.bizkredit.entity.Covenant;
import com.bizkredit.entity.CovenantTemplate;
import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.enums.CovenantStatus;
import com.bizkredit.enums.CovenantTemplateStatus;
import com.bizkredit.enums.CovenantType;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.CovenantRepository;
import com.bizkredit.repository.CovenantTemplateRepository;
import com.bizkredit.repository.FacilityAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CovenantTemplateService {

    private final CovenantTemplateRepository templateRepository;
    private final CovenantRepository covenantRepository;
    private final FacilityAccountRepository facilityRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CovenantTemplate createTemplate(CovenantTemplate template, Long createdById) {
        if (templateRepository.existsByTemplateName(template.getTemplateName())) {
            throw new BadRequestException("Template name already exists: " + template.getTemplateName());
        }
        template.setCreatedById(createdById);
        template.setStatus(CovenantTemplateStatus.ACTIVE);
        CovenantTemplate saved = templateRepository.save(template);
        auditLogService.log(createdById, "CREATE", "CovenantTemplate", String.valueOf(saved.getTemplateId()));
        log.info("Covenant template created: {}", saved.getTemplateName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CovenantTemplate> getTemplates(CovenantType covenantType) {
        if (covenantType != null) {
            return templateRepository.findByCovenantTypeAndStatus(covenantType, CovenantTemplateStatus.ACTIVE);
        }
        return templateRepository.findByStatus(CovenantTemplateStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public CovenantTemplate getById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
    }

    @Transactional
    public CovenantTemplate updateTemplate(Long templateId, CovenantTemplate updates) {
        CovenantTemplate existing = getById(templateId);
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getDefaultThresholdValue() != null) existing.setDefaultThresholdValue(updates.getDefaultThresholdValue());
        if (updates.getDefaultMonitoringFrequency() != null) existing.setDefaultMonitoringFrequency(updates.getDefaultMonitoringFrequency());
        if (updates.getApplicableProductTypes() != null) existing.setApplicableProductTypes(updates.getApplicableProductTypes());
        auditLogService.log(null, "UPDATE", "CovenantTemplate", String.valueOf(templateId));
        return templateRepository.save(existing);
    }

    // Apply template to a facility — creates a new Covenant with template defaults
    @Transactional
    public Covenant applyTemplate(Long templateId, Long facilityId) {
        CovenantTemplate template = getById(templateId);
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));

        Covenant covenant = Covenant.builder()
                .facility(facility)
                .covenantType(template.getCovenantType())
                .description(template.getDescription())
                .thresholdValue(template.getDefaultThresholdValue())
                .monitoringFrequency(template.getDefaultMonitoringFrequency())
                .status(CovenantStatus.ACTIVE)
                .build();

        Covenant saved = covenantRepository.save(covenant);
        auditLogService.log(null, "CREATE", "Covenant", String.valueOf(saved.getCovenantId()));
        log.info("Template {} applied to facility {}, created covenant {}", templateId, facilityId, saved.getCovenantId());
        return saved;
    }
}
