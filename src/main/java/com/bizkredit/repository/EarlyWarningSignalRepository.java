package com.bizkredit.repository;

import com.bizkredit.entity.EarlyWarningSignal;
import com.bizkredit.enums.EWSSeverity;
import com.bizkredit.enums.EWSStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EarlyWarningSignalRepository extends JpaRepository<EarlyWarningSignal, Long> {

    @EntityGraph(attributePaths = {"facility", "facility.business"})
    Optional<EarlyWarningSignal> findById(Long id);

    @EntityGraph(attributePaths = {"facility", "facility.business"})
    List<EarlyWarningSignal> findByFacility_FacilityId(Long facilityId);

    List<EarlyWarningSignal> findBySeverity(EWSSeverity severity);

    List<EarlyWarningSignal> findByStatus(EWSStatus status);
}
