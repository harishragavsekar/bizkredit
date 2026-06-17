package com.bizkredit.repository;

import com.bizkredit.entity.NPARecord;
import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NPARecordRepository extends JpaRepository<NPARecord, Long> {

    // EntityGraph needed on every method returning NPARecord directly to a controller —
    // `facility` is LAZY and open-in-view=false (same bug pattern as LoanApplicationRepository).
    @EntityGraph(attributePaths = {"facility"})
    Optional<NPARecord> findById(Long id);

    @EntityGraph(attributePaths = {"facility"})
    List<NPARecord> findByFacility_FacilityId(Long facilityId);

    @EntityGraph(attributePaths = {"facility"})
    List<NPARecord> findByProvisioningCategoryAndStatus(NPAProvisioningCategory category, NPARecordStatus status);

    @EntityGraph(attributePaths = {"facility"})
    List<NPARecord> findByStatus(NPARecordStatus status);

    @EntityGraph(attributePaths = {"facility"})
    Optional<NPARecord> findByFacility_FacilityIdAndStatus(Long facilityId, NPARecordStatus status);

    @EntityGraph(attributePaths = {"facility"})
    List<NPARecord> findAll();
}
