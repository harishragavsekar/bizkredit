package com.bizkredit.module5.repository;

import com.bizkredit.module5.entity.EarlyWarningSignal;
import com.bizkredit.enums.EWSSeverity;
import com.bizkredit.enums.EWSStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EarlyWarningSignalRepository extends JpaRepository<EarlyWarningSignal, Long> {

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    Optional<EarlyWarningSignal> findById(Long id);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<EarlyWarningSignal> findByFacility_FacilityId(Long facilityId);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<EarlyWarningSignal> findBySeverity(EWSSeverity severity);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<EarlyWarningSignal> findByStatus(EWSStatus status);
}