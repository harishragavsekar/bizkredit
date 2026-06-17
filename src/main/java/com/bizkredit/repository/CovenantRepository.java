package com.bizkredit.repository;

import com.bizkredit.entity.Covenant;
import com.bizkredit.enums.CovenantStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CovenantRepository extends JpaRepository<Covenant, Long> {

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    Optional<Covenant> findById(Long id);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<Covenant> findByFacility_FacilityId(Long facilityId);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<Covenant> findByStatus(CovenantStatus status);
}