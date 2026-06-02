package com.bizkredit.service;

import com.bizkredit.entity.ApplicationDocument;
import com.bizkredit.entity.LoanApplication;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.VerificationStatus;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.ApplicationDocumentRepository;
import com.bizkredit.repository.LoanApplicationRepository;
import com.bizkredit.repository.SMEBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final SMEBusinessRepository smeBusinessRepository;

    public LoanApplication submitApplication(Long businessId, LoanApplication application) {
        SMEBusiness business = smeBusinessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
        application.setBusiness(business);
        application.setStatus(ApplicationStatus.SUBMITTED);
        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Loan application submitted for business {}: applicationId={}", businessId, saved.getApplicationId());
        return saved;
    }

    public LoanApplication getApplicationById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
    }

    public List<LoanApplication> getApplicationsByBusiness(Long businessId) {
        return loanApplicationRepository.findByBusiness_BusinessId(businessId);
    }

    public List<LoanApplication> getApplicationsByStatus(ApplicationStatus status) {
        return loanApplicationRepository.findByStatus(status);
    }

    public LoanApplication updateStatus(Long applicationId, ApplicationStatus newStatus) {
        LoanApplication application = getApplicationById(applicationId);
        application.setStatus(newStatus);
        log.info("Application {} status updated to {}", applicationId, newStatus);
        return loanApplicationRepository.save(application);
    }

    public LoanApplication assignAnalyst(Long applicationId, Long analystId) {
        LoanApplication application = getApplicationById(applicationId);
        application.setAssignedAnalystId(analystId);
        application.setStatus(ApplicationStatus.IN_REVIEW);
        log.info("Analyst {} assigned to application {}", analystId, applicationId);
        return loanApplicationRepository.save(application);
    }

    public ApplicationDocument uploadDocument(Long applicationId, ApplicationDocument document) {
        LoanApplication application = getApplicationById(applicationId);
        document.setApplication(application);
        ApplicationDocument saved = documentRepository.save(document);
        log.info("Document uploaded for application {}: type={}", applicationId, saved.getDocumentType());
        return saved;
    }

    public List<ApplicationDocument> getDocumentsByApplication(Long applicationId) {
        getApplicationById(applicationId);
        return documentRepository.findByApplication_ApplicationId(applicationId);
    }

    public ApplicationDocument updateVerificationStatus(Long docId, VerificationStatus status) {
        ApplicationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        doc.setVerificationStatus(status);
        log.info("Document {} verification status updated to {}", docId, status);
        return documentRepository.save(doc);
    }
}
