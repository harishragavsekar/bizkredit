package com.bizkredit.repository;

import com.bizkredit.entity.CovenantTracking;
import com.bizkredit.enums.ComplianceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CovenantTrackingRepository extends JpaRepository<CovenantTracking, Long> {

    @EntityGraph(attributePaths = {"covenant", "covenant.facility"})
    List<CovenantTracking> findByCovenant_CovenantId(Long covenantId);

    List<CovenantTracking> findByComplianceStatus(ComplianceStatus status);
}
