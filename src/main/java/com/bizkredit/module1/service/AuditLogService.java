package com.bizkredit.module1.service;

import com.bizkredit.module1.entity.AuditLog;
import com.bizkredit.module1.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Helper method used by all services to log actions
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
    public Page<AuditLog> getAuditLogs(Long userId, String entityType, String action,
                                        LocalDateTime from, LocalDateTime to,
                                        int page, int size) {
        return auditLogRepository.findWithFilters(
                userId, entityType, action, from, to,
                PageRequest.of(page, size)
        );
    }

    @Transactional(readOnly = true)
    public AuditLog getById(Long auditId) {
        return auditLogRepository.findById(auditId)
                .orElseThrow(() -> new com.bizkredit.exception.ResourceNotFoundException(
                        "Audit log not found: " + auditId));
    }
}
