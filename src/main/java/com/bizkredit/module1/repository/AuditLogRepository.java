package com.bizkredit.module1.repository;

import com.bizkredit.module1.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // For pagination (used in controller)
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    // For simple list (used in UserService)
    List<AuditLog> findByUserId(Long userId);
}