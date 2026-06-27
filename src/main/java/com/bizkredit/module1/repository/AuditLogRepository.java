package com.bizkredit.module1.repository;

import com.bizkredit.module1.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByEntityTypeAndRecordId(String entityType, String recordId);

    // Filtered query with pagination - all params optional
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:from IS NULL OR a.timestamp >= :from) AND " +
           "(:to IS NULL OR a.timestamp <= :to) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(
            @Param("userId") Long userId,
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
