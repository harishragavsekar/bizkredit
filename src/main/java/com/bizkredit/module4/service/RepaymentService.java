package com.bizkredit.module4.service;

import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.module4.entity.FacilityAccount;
import com.bizkredit.module4.entity.Repayment;
import com.bizkredit.enums.DrawdownStatus;
import com.bizkredit.enums.RepaymentStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.module4.repository.DrawdownRepository;
import com.bizkredit.module4.repository.FacilityAccountRepository;
import com.bizkredit.module4.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final DrawdownRepository drawdownRepository;
    private final FacilityAccountRepository facilityRepository;

    @Transactional
    public Repayment recordRepayment(Repayment repayment) {
        Drawdown drawdown = drawdownRepository.findById(repayment.getDrawdown().getDrawdownId())
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found"));

        FacilityAccount facility = drawdown.getFacility();

        if (repayment.getPrincipalComponent() != null && repayment.getInterestComponent() != null) {
            BigDecimal total = repayment.getPrincipalComponent().add(repayment.getInterestComponent());

            if (total.compareTo(repayment.getAmount()) != 0) {
                throw new BadRequestException("Principal + Interest must equal total amount");
            }
        }

        BigDecimal alreadyRepaid = repaymentRepository.sumRepaidForDrawdown(drawdown.getDrawdownId());
        BigDecimal remaining = drawdown.getAmount().subtract(alreadyRepaid);

        if (repayment.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException("Repayment amount exceeds outstanding amount");
        }

        repayment.setDrawdown(drawdown);
        repayment.setFacility(facility);
        repayment.setStatus(RepaymentStatus.RECEIVED);

        facility.setOutstandingBalance(
                facility.getOutstandingBalance().subtract(repayment.getAmount())
        );
        facilityRepository.save(facility);

        Repayment saved = repaymentRepository.save(repayment);

        if (alreadyRepaid.add(repayment.getAmount()).compareTo(drawdown.getAmount()) >= 0) {
            drawdown.setStatus(DrawdownStatus.REPAID);
            drawdownRepository.save(drawdown);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Repayment> getByFacility(Long facilityId) {
        return repaymentRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional(readOnly = true)
    public List<Repayment> getByDrawdown(Long drawdownId) {
        return repaymentRepository.findByDrawdown_DrawdownId(drawdownId);
    }

    @Transactional(readOnly = true)
    public Repayment getById(Long repaymentId) {
        return repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found"));
    }

    @Transactional
    public Repayment verifyRepayment(Long repaymentId, Long verifiedById) {
        Repayment repayment = getById(repaymentId);

        if (repayment.getRecordedById() != null && repayment.getRecordedById().equals(verifiedById)) {
            throw new BadRequestException("Verifier cannot be same as recorder");
        }

        repayment.setStatus(RepaymentStatus.VERIFIED);
        repayment.setVerifiedById(verifiedById);

        return repaymentRepository.save(repayment);
    }
}