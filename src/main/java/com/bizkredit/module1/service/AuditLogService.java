package com.bizkredit.module1.service;

import com.bizkredit.common.exception.ResourceNotFoundException;
import com.bizkredit.module1.entity.AuditLog;
import com.bizkredit.module1.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, String recordId) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .recordId(recordId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByUserId(Long userId, int page, int size) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(
                userId,
                PageRequest.of(page, size)
        );
    }

    @Transactional(readOnly = true)
    public AuditLog getById(Long auditId) {
        return auditLogRepository.findById(auditId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Audit log not found: " + auditId));
    }
}
