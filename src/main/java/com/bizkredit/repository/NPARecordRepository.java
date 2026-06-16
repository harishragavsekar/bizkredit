package com.bizkredit.repository;

import com.bizkredit.entity.NPARecord;
import com.bizkredit.enums.NPAProvisioningCategory;
import com.bizkredit.enums.NPARecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NPARecordRepository extends JpaRepository<NPARecord, Long> {

    List<NPARecord> findByFacility_FacilityId(Long facilityId);

    List<NPARecord> findByProvisioningCategoryAndStatus(NPAProvisioningCategory category, NPARecordStatus status);

    List<NPARecord> findByStatus(NPARecordStatus status);

    Optional<NPARecord> findByFacility_FacilityIdAndStatus(Long facilityId, NPARecordStatus status);
}
