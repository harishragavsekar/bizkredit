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

// Service for SME Business Profile (4.2) and Loan Application (4.3)
// Uses Java 21 features: var, switch expressions
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

    public SMEBusiness registerBusiness(SMEBusiness business) {
        if (businessRepository.existsByRegistrationNumber(business.getRegistrationNumber())) {
            throw new BadRequestException("Business already registered: " + business.getRegistrationNumber());
        }
        var saved = businessRepository.save(business);
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

    public SMEBusiness updateKycStatus(Long businessId, String kycStatus) {
        var business = getBusinessById(businessId);

        // Java 14+ switch expression - validates KYC status
        var validStatus = switch (kycStatus) {
            case "Pending", "Verified", "Rejected" -> kycStatus;
            default -> throw new BadRequestException("Invalid KYC status: " + kycStatus);
        };

        business.setKycStatus(validStatus);
        log.info("KYC status updated for business {}: {}", businessId, kycStatus);
        return businessRepository.save(business);
    }

    public Promoter addPromoter(Long businessId, Promoter promoter) {
        var business = getBusinessById(businessId);
        promoter.setBusiness(business);
        var saved = promoterRepository.save(promoter);
        log.info("Promoter {} added to business {}", saved.getName(), businessId);
        return saved;
    }

    public List<Promoter> getPromotersByBusiness(Long businessId) {
        getBusinessById(businessId);
        return promoterRepository.findByBusiness_BusinessId(businessId);
    }

    public GroupCompany linkGroupCompany(Long parentId, Long subsidiaryId, String relationship) {
        if (parentId.equals(subsidiaryId)) {
            throw new BadRequestException("Parent and subsidiary cannot be the same business");
        }
        var parent = getBusinessById(parentId);
        var subsidiary = getBusinessById(subsidiaryId);

        var link = GroupCompany.builder()
                .parentBusiness(parent)
                .subsidiaryBusiness(subsidiary)
                .relationship(relationship)
                .build();
        log.info("Group link: {} -> {} [{}]", parentId, subsidiaryId, relationship);
        return groupCompanyRepository.save(link);
    }

    // ── 4.3 Loan Application ──────────────────────────────────────

    public LoanApplication submitApplication(Long businessId, LoanApplication application) {
        var business = getBusinessById(businessId);
        application.setBusiness(business);
        application.setStatus(ApplicationStatus.SUBMITTED);
        var saved = loanApplicationRepository.save(application);
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

    public LoanApplication assignAnalyst(Long applicationId, Long analystId) {
        var application = getApplicationById(applicationId);
        application.setAssignedAnalystId(analystId);
        application.setStatus(ApplicationStatus.IN_REVIEW);
        log.info("Analyst {} assigned to application {}", analystId, applicationId);
        return loanApplicationRepository.save(application);
    }

    public LoanApplication updateStatus(Long applicationId, ApplicationStatus status) {
        var application = getApplicationById(applicationId);
        application.setStatus(status);
        return loanApplicationRepository.save(application);
    }

    public ApplicationDocument uploadDocument(Long applicationId, ApplicationDocument document) {
        var application = getApplicationById(applicationId);
        document.setApplication(application);
        var saved = documentRepository.save(document);
        log.info("Document uploaded for application {}: {}", applicationId, saved.getDocumentType());
        return saved;
    }

    public List<ApplicationDocument> getDocumentsByApplication(Long applicationId) {
        getApplicationById(applicationId);
        return documentRepository.findByApplication_ApplicationId(applicationId);
    }

    public ApplicationDocument verifyDocument(Long docId, VerificationStatus status) {
        var doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(status);
        log.info("Document {} status updated to {}", docId, status);
        return documentRepository.save(doc);
    }
}
