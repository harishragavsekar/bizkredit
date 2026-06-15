package com.bizkredit.repository;

import com.bizkredit.entity.CollateralRecord;
import com.bizkredit.enums.CollateralStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollateralRecordRepository extends JpaRepository<CollateralRecord, Long> {

    @EntityGraph(attributePaths = {"application", "application.business"})
    Optional<CollateralRecord> findById(Long id);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<CollateralRecord> findByApplication_ApplicationId(Long applicationId);

    List<CollateralRecord> findByStatus(CollateralStatus status);
}