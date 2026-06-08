package com.bizkredit.repository;

import com.bizkredit.entity.CollateralRecord;
import com.bizkredit.enums.CollateralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollateralRecordRepository extends JpaRepository<CollateralRecord, Long> {

    List<CollateralRecord> findByApplication_ApplicationId(Long applicationId);

    List<CollateralRecord> findByStatus(CollateralStatus status);
}
