package com.bizkredit.service;

import com.bizkredit.entity.Drawdown;
import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.entity.Repayment;
import com.bizkredit.enums.DrawdownStatus;
import com.bizkredit.enums.NotificationCategory;
import com.bizkredit.enums.RepaymentStatus;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.DrawdownRepository;
import com.bizkredit.repository.FacilityAccountRepository;
import com.bizkredit.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final DrawdownRepository drawdownRepository;
    private final FacilityAccountRepository facilityRepository;
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;

    @Transactional
    public Repayment recordRepayment(Repayment repayment) {
        Drawdown drawdown = drawdownRepository.findById(repayment.getDrawdown().getDrawdownId())
                .orElseThrow(() -> new ResourceNotFoundException("Drawdown not found"));
        FacilityAccount facility = drawdown.getFacility();

        // Validate components sum to total
        if (repayment.getPrincipalComponent() != null && repayment.getInterestComponent() != null) {
            BigDecimal sum = repayment.getPrincipalComponent().add(repayment.getInterestComponent());
            if (sum.compareTo(repayment.getAmount()) != 0) {
                throw new BadRequestException("Principal + Interest must equal total amount");
            }
        }

        // Validate amount does not exceed outstanding
        BigDecimal alreadyRepaid = repaymentRepository.sumRepaidForDrawdown(drawdown.getDrawdownId());
        BigDecimal remaining = drawdown.getAmount().subtract(alreadyRepaid);
        if (repayment.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException("Repayment amount exceeds drawdown outstanding of " + remaining);
        }

        repayment.setDrawdown(drawdown);
        repayment.setFacility(facility);
        repayment.setStatus(RepaymentStatus.RECEIVED);

        // Reduce outstanding balance
        facility.setOutstandingBalance(facility.getOutstandingBalance().subtract(repayment.getAmount()));
        facilityRepository.save(facility);

        Repayment saved = repaymentRepository.save(repayment);
        auditLogService.log(repayment.getRecordedById(), "CREATE", "Repayment", String.valueOf(saved.getRepaymentId()));

        // Check if drawdown is fully repaid
        BigDecimal totalRepaid = alreadyRepaid.add(repayment.getAmount());
        if (totalRepaid.compareTo(drawdown.getAmount()) >= 0) {
            drawdown.setStatus(DrawdownStatus.REPAID);
            drawdownRepository.save(drawdown);
            log.info("Drawdown {} fully repaid", drawdown.getDrawdownId());
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
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found: " + repaymentId));
    }

    // PUT /repayments/{id}/verify — maker-checker: verifier must be different from recorder
    @Transactional
    public Repayment verifyRepayment(Long repaymentId, Long verifiedById) {
        Repayment repayment = getById(repaymentId);
        if (repayment.getRecordedById() != null && repayment.getRecordedById().equals(verifiedById)) {
            throw new BadRequestException("Verifier cannot be the same user who recorded the repayment");
        }
        repayment.setStatus(RepaymentStatus.VERIFIED);
        repayment.setVerifiedById(verifiedById);
        auditLogService.log(verifiedById, "APPROVE", "Repayment", String.valueOf(repaymentId));
        log.info("Repayment {} verified by {}", repaymentId, verifiedById);
        return repaymentRepository.save(repayment);
    }

    // PUT /repayments/{id}/reverse — reverse erroneous repayment
    @Transactional
    public Repayment reverseRepayment(Long repaymentId) {
        Repayment repayment = getById(repaymentId);
        if (repayment.getStatus() == RepaymentStatus.REVERSED) {
            throw new BadRequestException("Repayment already reversed");
        }
        repayment.setStatus(RepaymentStatus.REVERSED);

        // Restore outstanding balance
        FacilityAccount facility = repayment.getFacility();
        facility.setOutstandingBalance(facility.getOutstandingBalance().add(repayment.getAmount()));
        facilityRepository.save(facility);

        // If drawdown was marked repaid, revert it to DISBURSED
        Drawdown drawdown = repayment.getDrawdown();
        if (drawdown.getStatus() == DrawdownStatus.REPAID) {
            drawdown.setStatus(DrawdownStatus.DISBURSED);
            drawdownRepository.save(drawdown);
        }

        auditLogService.log(null, "UPDATE", "Repayment", String.valueOf(repaymentId));
        log.info("Repayment {} reversed", repaymentId);
        return repaymentRepository.save(repayment);
    }
}
