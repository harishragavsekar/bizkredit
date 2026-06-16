package com.bizkredit.service;

import com.bizkredit.dto.MakerCheckerDTOs.MakerCheckerRequest;
import com.bizkredit.entity.MakerCheckerRecord;
import com.bizkredit.enums.MakerCheckerStatus;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.MakerCheckerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MakerCheckerService {

    private final MakerCheckerRepository repo;

    // ── Maker submits an action for checker approval ──────────────

    @Transactional
    public MakerCheckerRecord submit(MakerCheckerRequest request) {
        String maker = currentUsername();

        // A user cannot be both maker and checker for the same record
        MakerCheckerRecord record = MakerCheckerRecord.builder()
                .entityType(request.entityType())
                .entityId(request.entityId())
                .action(request.action())
                .payloadJson(request.payloadJson())
                .submittedBy(maker)
                .requiredCheckerRole(request.requiredCheckerRole())
                .status(MakerCheckerStatus.PENDING_APPROVAL)
                .build();

        log.info("Maker-Checker: {} submitted {} on {} id={}",
                maker, request.action(), request.entityType(), request.entityId());
        return repo.save(record);
    }

    // ── Checker approves ─────────────────────────────────────────

    @Transactional
    public MakerCheckerRecord approve(Long id, String comments) {
        MakerCheckerRecord record = getOrThrow(id);
        validatePending(record);
        validateNotSelf(record);

        record.setStatus(MakerCheckerStatus.APPROVED);
        record.setCheckedBy(currentUsername());
        record.setCheckerComments(comments);

        log.info("Maker-Checker: {} APPROVED record id={}", currentUsername(), id);
        return repo.save(record);
    }

    // ── Checker rejects ──────────────────────────────────────────

    @Transactional
    public MakerCheckerRecord reject(Long id, String comments) {
        MakerCheckerRecord record = getOrThrow(id);
        validatePending(record);
        validateNotSelf(record);

        record.setStatus(MakerCheckerStatus.REJECTED);
        record.setCheckedBy(currentUsername());
        record.setCheckerComments(comments);

        log.info("Maker-Checker: {} REJECTED record id={}", currentUsername(), id);
        return repo.save(record);
    }

    // ── Maker cancels their own pending submission ────────────────

    @Transactional
    public MakerCheckerRecord cancel(Long id) {
        MakerCheckerRecord record = getOrThrow(id);
        validatePending(record);

        if (!record.getSubmittedBy().equals(currentUsername())) {
            throw new IllegalStateException("Only the original maker can cancel this record.");
        }

        record.setStatus(MakerCheckerStatus.CANCELLED);
        return repo.save(record);
    }

    // ── Queries ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MakerCheckerRecord> getPendingForRole(String role) {
        return repo.findByRequiredCheckerRoleAndStatus(role, MakerCheckerStatus.PENDING_APPROVAL);
    }

    @Transactional(readOnly = true)
    public List<MakerCheckerRecord> getMySubmissions() {
        return repo.findBySubmittedBy(currentUsername());
    }

    @Transactional(readOnly = true)
    public List<MakerCheckerRecord> getAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public MakerCheckerRecord getById(Long id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<MakerCheckerRecord> getByEntity(String entityType, Long entityId) {
        return repo.findByEntityTypeAndEntityId(entityType, entityId);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private MakerCheckerRecord getOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MakerCheckerRecord not found: " + id));
    }

    private void validatePending(MakerCheckerRecord record) {
        if (record.getStatus() != MakerCheckerStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Record is not in PENDING_APPROVAL state. Current state: " + record.getStatus());
        }
    }

    private void validateNotSelf(MakerCheckerRecord record) {
        if (record.getSubmittedBy().equals(currentUsername())) {
            throw new IllegalStateException(
                    "Maker and checker cannot be the same user. Self-approval is not permitted.");
        }
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
