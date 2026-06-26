package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.NotificationCategory;
import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMELoanService {

    private final SMEBusinessRepository businessRepository;
    private final PromoterRepository promoterRepository;
    private final GroupCompanyRepository groupCompanyRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;

    // 4.2 SME Business

    @Transactional
    public SMEBusiness registerBusiness(SMEBusiness business) {
        if (businessRepository.existsByRegistrationNumber(business.getRegistrationNumber())) {
            throw new BadRequestException("Business already registered: " + business.getRegistrationNumber());
        }
        SMEBusiness saved = businessRepository.save(business);
        log.info("Business registered: {} [{}]", saved.getBusinessName(), saved.getRegistrationNumber());
        auditLogService.log(null, "CREATE", "SMEBusiness", String.valueOf(saved.getBusinessId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public SMEBusiness getBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }

    @Transactional(readOnly = true)
    public List<SMEBusiness> getAllBusinesses() {
        return businessRepository.findAll();
    }

    // GET /api/businesses with optional filters
    @Transactional(readOnly = true)
    public List<SMEBusiness> getBusinessesFiltered(String entityType, String industry, String status) {
        List<SMEBusiness> all = businessRepository.findAll();
        return all.stream()
                .filter(b -> entityType == null || (b.getEntityType() != null && b.getEntityType().name().equals(entityType)))
                .filter(b -> industry == null || industry.equalsIgnoreCase(b.getIndustry()))
                .filter(b -> status == null || status.equalsIgnoreCase(b.getStatus()))
                .toList();
    }

    // PUT /api/businesses/{id} — update profile
    @Transactional
    public SMEBusiness updateBusiness(Long businessId, SMEBusiness updates) {
        SMEBusiness existing = getBusinessById(businessId);

        // Only update non-null fields
        if (updates.getBusinessName() != null) existing.setBusinessName(updates.getBusinessName());
        if (updates.getIndustry() != null) existing.setIndustry(updates.getIndustry());
        if (updates.getAnnualTurnover() != null) existing.setAnnualTurnover(updates.getAnnualTurnover());
        if (updates.getEmployeeCount() != null) existing.setEmployeeCount(updates.getEmployeeCount());
        if (updates.getYearsInOperation() != null) existing.setYearsInOperation(updates.getYearsInOperation());
        if (updates.getPrimaryBankId() != null) existing.setPrimaryBankId(updates.getPrimaryBankId());

        SMEBusiness saved = businessRepository.save(existing);
        auditLogService.log(null, "UPDATE", "SMEBusiness", String.valueOf(businessId));
        log.info("Business {} updated", businessId);
        return saved;
    }

    // PATCH /api/businesses/{id}/status
    @Transactional
    public SMEBusiness updateBusinessStatus(Long businessId, String status) {
        SMEBusiness business = getBusinessById(businessId);
        String validStatus = switch (status) {
            case "Active", "Inactive", "Blacklisted" -> status;
            default -> throw new BadRequestException("Invalid status. Must be Active, Inactive, or Blacklisted");
        };
        business.setStatus(validStatus);
        auditLogService.log(null, "STATUS_CHANGE", "SMEBusiness", String.valueOf(businessId));
        return businessRepository.save(business);
    }

    @Transactional
    public SMEBusiness updateKycStatus(Long businessId, String kycStatus) {
        SMEBusiness business = getBusinessById(businessId);
        String validStatus = switch (kycStatus) {
            case "Pending", "Verified", "Rejected" -> kycStatus;
            default -> throw new BadRequestException("Invalid KYC status: " + kycStatus);
        };
        business.setKycStatus(validStatus);
        auditLogService.log(null, "UPDATE", "SMEBusiness", String.valueOf(businessId));
        log.info("KYC status updated for business {}: {}", businessId, kycStatus);
        return businessRepository.save(business);
    }

    @Transactional
    public Promoter addPromoter(Long businessId, Promoter promoter) {
        SMEBusiness business = getBusinessById(businessId);
        promoter.setBusiness(business);
        Promoter saved = promoterRepository.save(promoter);
        auditLogService.log(null, "CREATE", "Promoter", String.valueOf(saved.getPromoterId()));
        log.info("Promoter {} added to business {}", saved.getName(), businessId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Promoter> getPromotersByBusiness(Long businessId) {
        getBusinessById(businessId);
        return promoterRepository.findByBusiness_BusinessId(businessId);
    }

    @Transactional
    public GroupCompany linkGroupCompany(Long parentId, Long subsidiaryId, String relationship) {
        if (parentId.equals(subsidiaryId)) {
            throw new BadRequestException("Parent and subsidiary cannot be the same business");
        }
        SMEBusiness parent = getBusinessById(parentId);
        SMEBusiness subsidiary = getBusinessById(subsidiaryId);
        GroupCompany link = GroupCompany.builder()
                .parentBusiness(parent)
                .subsidiaryBusiness(subsidiary)
                .relationship(relationship)
                .build();
        log.info("Group link: {} -> {} [{}]", parentId, subsidiaryId, relationship);
        return groupCompanyRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<GroupCompany> getGroupCompaniesByBusiness(Long businessId) {
        return groupCompanyRepository.findByParentBusiness_BusinessId(businessId);
    }

    // ── 4.3 Loan Application ──────────────────────────────────────

    // Creates application in DRAFT status
    @Transactional
    public LoanApplication createApplication(Long businessId, LoanApplication application) {
        SMEBusiness business = getBusinessById(businessId);
        application.setBusiness(business);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setApplicationDate(LocalDate.now());
        LoanApplication saved = loanApplicationRepository.save(application);
        auditLogService.log(null, "CREATE", "LoanApplication", String.valueOf(saved.getApplicationId()));
        log.info("Application created for business {}: id={}", businessId, saved.getApplicationId());
        return saved;
    }

    // Legacy submit — kept for backward compat, delegates to createApplication
    @Transactional
    public LoanApplication submitApplication(Long businessId, LoanApplication application) {
        SMEBusiness business = getBusinessById(businessId);
        application.setBusiness(business);
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setApplicationDate(LocalDate.now());
        LoanApplication saved = loanApplicationRepository.save(application);
        auditLogService.log(null, "CREATE", "LoanApplication", String.valueOf(saved.getApplicationId()));
        return saved;
    }

    // PATCH /api/applications/{id}/submit — explicit Draft → Submitted transition
    @Transactional
    public LoanApplication submitDraftApplication(Long applicationId) {
        LoanApplication app = getApplicationById(applicationId);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT applications can be submitted");
        }
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setApplicationDate(LocalDate.now());
        auditLogService.log(null, "STATUS_CHANGE", "LoanApplication", String.valueOf(applicationId));
        log.info("Application {} submitted", applicationId);
        return loanApplicationRepository.save(app);
    }

    // PUT /api/applications/{id} — update (Draft only)
    @Transactional
    public LoanApplication updateApplication(Long applicationId, LoanApplication updates) {
        LoanApplication existing = getApplicationById(applicationId);
        if (existing.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT applications can be updated");
        }
        if (updates.getProductType() != null) existing.setProductType(updates.getProductType());
        if (updates.getRequestedAmount() != null) existing.setRequestedAmount(updates.getRequestedAmount());
        if (updates.getTenure() != null) existing.setTenure(updates.getTenure());
        if (updates.getPurpose() != null) existing.setPurpose(updates.getPurpose());
        auditLogService.log(null, "UPDATE", "LoanApplication", String.valueOf(applicationId));
        return loanApplicationRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public LoanApplication getApplicationById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> getApplicationsByBusiness(Long businessId) {
        return loanApplicationRepository.findByBusiness_BusinessId(businessId);
    }

    // GET /api/applications with filters
    @Transactional(readOnly = true)
    public List<LoanApplication> getApplicationsFiltered(Long businessId, ApplicationStatus status, ProductType productType) {
        return loanApplicationRepository.findWithFilters(businessId, status, productType);
    }

    @Transactional
    public LoanApplication assignAnalyst(Long applicationId, Long analystId) {
        LoanApplication application = getApplicationById(applicationId);
        application.setAssignedAnalystId(analystId);
        application.setStatus(ApplicationStatus.IN_REVIEW);
        auditLogService.log(null, "UPDATE", "LoanApplication", String.valueOf(applicationId));
        log.info("Analyst {} assigned to application {}", analystId, applicationId);
        return loanApplicationRepository.save(application);
    }

    // Enforces status workflow transitions
    @Transactional
    public LoanApplication updateStatus(Long applicationId, ApplicationStatus newStatus) {
        LoanApplication application = getApplicationById(applicationId);
        validateStatusTransition(application.getStatus(), newStatus);
        application.setStatus(newStatus);
        auditLogService.log(null, "STATUS_CHANGE", "LoanApplication", String.valueOf(applicationId));
        // Notify analyst if status changes beyond submitted
        if (application.getAssignedAnalystId() != null) {
            notificationHelper.notify(application.getAssignedAnalystId(),
                    "Application #" + applicationId + " status changed to " + newStatus,
                    NotificationCategory.APPLICATION);
        }
        return loanApplicationRepository.save(application);
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == ApplicationStatus.SUBMITTED;
            case SUBMITTED -> next == ApplicationStatus.IN_REVIEW || next == ApplicationStatus.REJECTED;
            case IN_REVIEW -> next == ApplicationStatus.UNDERWRITING_APPROVAL || next == ApplicationStatus.REJECTED;
            case UNDERWRITING_APPROVAL -> next == ApplicationStatus.SANCTIONED || next == ApplicationStatus.REJECTED;
            case SANCTIONED -> next == ApplicationStatus.DISBURSED;
            case REJECTED, DISBURSED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid status transition: " + current + " → " + next);
        }
    }

    @Transactional
    public ApplicationDocument uploadDocument(Long applicationId, ApplicationDocument document) {
        LoanApplication application = getApplicationById(applicationId);
        document.setApplication(application);
        ApplicationDocument saved = documentRepository.save(document);
        auditLogService.log(null, "CREATE", "ApplicationDocument", String.valueOf(saved.getDocId()));
        log.info("Document uploaded for application {}: {}", applicationId, saved.getDocumentType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ApplicationDocument> getDocumentsByApplication(Long applicationId) {
        getApplicationById(applicationId);
        return documentRepository.findByApplication_ApplicationId(applicationId);
    }

    @Transactional
    public ApplicationDocument verifyDocument(Long docId, VerificationStatus status) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(status);
        auditLogService.log(null, "STATUS_CHANGE", "ApplicationDocument", String.valueOf(docId));
        log.info("Document {} status updated to {}", docId, status);
        return documentRepository.save(doc);
    }

    // PATCH /documents/{docId}/flag-deficient — flags with reason, notifies applicant
    @Transactional
    public ApplicationDocument flagDeficient(Long docId, String reason) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(VerificationStatus.DEFICIENT);
        auditLogService.log(null, "STATUS_CHANGE", "ApplicationDocument", String.valueOf(docId));
        log.info("Document {} flagged as deficient: {}", docId, reason);
        // Notify assigned analyst of the application
        if (doc.getApplication() != null && doc.getApplication().getAssignedAnalystId() != null) {
            notificationHelper.notify(doc.getApplication().getAssignedAnalystId(),
                    "Document " + doc.getDocumentType() + " flagged deficient: " + reason,
                    NotificationCategory.APPLICATION);
        }
        return documentRepository.save(doc);
    }

    // PATCH /documents/{docId}/reject
    @Transactional
    public ApplicationDocument rejectDocument(Long docId, String reason) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(VerificationStatus.REJECTED);
        auditLogService.log(null, "STATUS_CHANGE", "ApplicationDocument", String.valueOf(docId));
        log.info("Document {} rejected: {}", docId, reason);
        return documentRepository.save(doc);
    }

    // DELETE /documents/{docId} — re-upload (resets to PENDING)
    @Transactional
    public void deleteDocument(Long docId) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        if (doc.getVerificationStatus() != VerificationStatus.DEFICIENT) {
            throw new BadRequestException("Only DEFICIENT documents can be deleted for re-upload");
        }
        documentRepository.deleteById(docId);
        auditLogService.log(null, "DELETE", "ApplicationDocument", String.valueOf(docId));
        log.info("Document {} deleted for re-upload", docId);
    }

    // GET single document by ID
    @Transactional(readOnly = true)
    public ApplicationDocument getDocumentById(Long docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
    }

    // UPDATE promoter
    @Transactional
    public Promoter updatePromoter(Long promoterId, Promoter updates) {
        Promoter existing = promoterRepository.findById(promoterId)
                .orElseThrow(() -> new ResourceNotFoundException("Promoter not found: " + promoterId));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getNationalIdRef() != null) existing.setNationalIdRef(updates.getNationalIdRef());
        if (updates.getShareholdingPercent() != null) existing.setShareholdingPercent(updates.getShareholdingPercent());
        if (updates.getPersonalNetWorth() != null) existing.setPersonalNetWorth(updates.getPersonalNetWorth());
        if (updates.getCreditScore() != null) existing.setCreditScore(updates.getCreditScore());
        auditLogService.log(null, "UPDATE", "Promoter", String.valueOf(promoterId));
        return promoterRepository.save(existing);
    }

    // SOFT-DELETE promoter
    @Transactional
    public void deletePromoter(Long promoterId) {
        Promoter existing = promoterRepository.findById(promoterId)
                .orElseThrow(() -> new ResourceNotFoundException("Promoter not found: " + promoterId));
        existing.setStatus("INACTIVE");
        promoterRepository.save(existing);
        auditLogService.log(null, "DELETE", "Promoter", String.valueOf(promoterId));
        log.info("Promoter {} soft-deleted", promoterId);
    }
}
