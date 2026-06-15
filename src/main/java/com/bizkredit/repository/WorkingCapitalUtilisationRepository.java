package com.bizkredit.repository;

import com.bizkredit.entity.WorkingCapitalUtilisation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingCapitalUtilisationRepository extends JpaRepository<WorkingCapitalUtilisation, Long> {

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    Optional<WorkingCapitalUtilisation> findById(Long id);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<WorkingCapitalUtilisation> findByFacility_FacilityId(Long facilityId);
}