package com.bizkredit.module4.repository;

import com.bizkredit.module4.entity.MakerCheckerRecord;
import com.bizkredit.enums.MakerCheckerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MakerCheckerRepository extends JpaRepository<MakerCheckerRecord, Long> {

    List<MakerCheckerRecord> findByStatus(MakerCheckerStatus status);

    List<MakerCheckerRecord> findByRequiredCheckerRoleAndStatus(String role, MakerCheckerStatus status);

    List<MakerCheckerRecord> findBySubmittedBy(String username);

    List<MakerCheckerRecord> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
