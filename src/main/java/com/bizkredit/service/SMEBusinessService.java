package com.bizkredit.service;

import com.bizkredit.entity.GroupCompany;
import com.bizkredit.entity.Promoter;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.GroupCompanyRepository;
import com.bizkredit.repository.PromoterRepository;
import com.bizkredit.repository.SMEBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMEBusinessService {

    private final SMEBusinessRepository smeBusinessRepository;
    private final PromoterRepository promoterRepository;
    private final GroupCompanyRepository groupCompanyRepository;

    // ── SMEBusiness ───────────────────────────────────────────────

    public SMEBusiness registerBusiness(SMEBusiness business) {
        if (smeBusinessRepository.existsByRegistrationNumber(business.getRegistrationNumber())) {
            throw new BadRequestException("Business already registered: " + business.getRegistrationNumber());
        }
        SMEBusiness saved = smeBusinessRepository.save(business);
        log.info("SME business registered: {} [{}]", saved.getBusinessName(), saved.getRegistrationNumber());
        return saved;
    }

    public SMEBusiness getBusinessById(Long businessId) {
        return smeBusinessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }

    public List<SMEBusiness> getAllBusinesses() {
        return smeBusinessRepository.findAll();
    }

    public SMEBusiness updateKycStatus(Long businessId, String kycStatus) {
        SMEBusiness business = getBusinessById(businessId);
        business.setKycStatus(kycStatus);
        log.info("KYC status updated for business {}: {}", businessId, kycStatus);
        return smeBusinessRepository.save(business);
    }

    public SMEBusiness updateStatus(Long businessId, String status) {
        SMEBusiness business = getBusinessById(businessId);
        business.setStatus(status);
        log.info("Business status updated for {}: {}", businessId, status);
        return smeBusinessRepository.save(business);
    }

    // ── Promoter ──────────────────────────────────────────────────

    public Promoter addPromoter(Long businessId, Promoter promoter) {
        SMEBusiness business = getBusinessById(businessId);
        promoter.setBusiness(business);
        Promoter saved = promoterRepository.save(promoter);
        log.info("Promoter {} added to business {}", saved.getName(), businessId);
        return saved;
    }

    public List<Promoter> getPromotersByBusiness(Long businessId) {
        getBusinessById(businessId); // validates business exists
        return promoterRepository.findByBusiness_BusinessId(businessId);
    }

    // ── GroupCompany ──────────────────────────────────────────────

    public GroupCompany linkGroupCompany(Long parentId, Long subsidiaryId, String relationship) {
        SMEBusiness parent = getBusinessById(parentId);
        SMEBusiness subsidiary = getBusinessById(subsidiaryId);

        if (parentId.equals(subsidiaryId)) {
            throw new BadRequestException("Parent and subsidiary cannot be the same business");
        }

        GroupCompany link = GroupCompany.builder()
                .parentBusiness(parent)
                .subsidiaryBusiness(subsidiary)
                .relationship(relationship)
                .build();

        log.info("Group link created: {} -> {} [{}]", parentId, subsidiaryId, relationship);
        return groupCompanyRepository.save(link);
    }

    public List<GroupCompany> getGroupsByParent(Long parentBusinessId) {
        return groupCompanyRepository.findByParentBusiness_BusinessId(parentBusinessId);
    }
}
