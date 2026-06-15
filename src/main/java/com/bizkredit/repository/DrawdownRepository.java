package com.bizkredit.repository;

import com.bizkredit.entity.Drawdown;
import com.bizkredit.enums.DrawdownStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawdownRepository extends JpaRepository<Drawdown, Long> {

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    Optional<Drawdown> findById(Long id);

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<Drawdown> findByFacility_FacilityId(Long facilityId);

    List<Drawdown> findByStatus(DrawdownStatus status);
}