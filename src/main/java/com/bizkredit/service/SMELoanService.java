package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

// Service for both SME Business Profile (4.2) and Loan Application (4.3)
// These are combined since loan applications belong to a business
@Slf4j
@Service
@RequiredArgsConstructor
public class SMELoanService {

    private final SMEBusinessRepository businessRepository;
    private final PromoterRepository promoterRepository;
    private final GroupCompanyRepository groupCompanyRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationDocumentRepository documentRepository;

    // ── 4.2 SME Business ─────────────────────────────────────────

    // Register a new SME business - checks for duplicate registration number
    public SMEBusiness registerBusiness(SMEBusiness business) {
        if (businessRepository.existsByRegistrationNumber(business.getRegistrationNumber())) {
            throw new BadRequestException("Business already registered: " + business.getRegistrationNumber());
        }
        SMEBusiness saved = businessRepository.save(business);
        log.info("Business registered: {} [{}]", saved.getBusinessName(), saved.getRegistrationNumber());
        return saved;
    }

    public SMEBusiness getBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }

    public List<SMEBusiness> getAllBusinesses() {
        return businessRepository.findAll();
    }

    // Update KYC status - Pending / Verified / Rejected
    public SMEBusiness updateKycStatus(Long businessId, String kycStatus) {
        SMEBusiness business = getBusinessById(businessId);
        business.setKycStatus(kycStatus);
        log.info("KYC status updated for business {}: {}", businessId, kycStatus);
        return businessRepository.save(business);
    }

    // Add a promoter/director to a business
    public Promoter addPromoter(Long businessId, Promoter promoter) {
        SMEBusiness business = getBusinessById(businessId);
        promoter.setBusiness(business);
        Promoter saved = promoterRepository.save(promoter);
        log.info("Promoter {} added to business {}", saved.getName(), businessId);
        return saved;
    }

    public List<Promoter> getPromotersByBusiness(Long businessId) {
        getBusinessById(businessId);
        return promoterRepository.findByBusiness_BusinessId(businessId);
    }

    // Link two businesses as parent-subsidiary
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

    // ── 4.3 Loan Application ──────────────────────────────────────

    // Submit a loan application for a business
    public LoanApplication submitApplication(Long businessId, LoanApplication application) {
        SMEBusiness business = getBusinessById(businessId);
        application.setBusiness(business);
        application.setStatus(ApplicationStatus.SUBMITTED);
        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Application submitted for business {}: id={}", businessId, saved.getApplicationId());
        return saved;
    }

    public LoanApplication getApplicationById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
    }

    public List<LoanApplication> getApplicationsByBusiness(Long businessId) {
        return loanApplicationRepository.findByBusiness_BusinessId(businessId);
    }

    // Assign a credit analyst to an application
    public LoanApplication assignAnalyst(Long applicationId, Long analystId) {
        LoanApplication application = getApplicationById(applicationId);
        application.setAssignedAnalystId(analystId);
        application.setStatus(ApplicationStatus.IN_REVIEW);
        log.info("Analyst {} assigned to application {}", analystId, applicationId);
        return loanApplicationRepository.save(application);
    }

    public LoanApplication updateStatus(Long applicationId, ApplicationStatus status) {
        LoanApplication application = getApplicationById(applicationId);
        application.setStatus(status);
        return loanApplicationRepository.save(application);
    }

    // Upload a document against a loan application
    public ApplicationDocument uploadDocument(Long applicationId, ApplicationDocument document) {
        LoanApplication application = getApplicationById(applicationId);
        document.setApplication(application);
        ApplicationDocument saved = documentRepository.save(document);
        log.info("Document uploaded for application {}: {}", applicationId, saved.getDocumentType());
        return saved;
    }

    public List<ApplicationDocument> getDocumentsByApplication(Long applicationId) {
        getApplicationById(applicationId);
        return documentRepository.findByApplication_ApplicationId(applicationId);
    }

    // Update document verification status
    public ApplicationDocument verifyDocument(Long docId, VerificationStatus status) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(status);
        log.info("Document {} status updated to {}", docId, status);
        return documentRepository.save(doc);
    }
}
