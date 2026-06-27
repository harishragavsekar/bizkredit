package com.bizkredit.module4.repository;

import com.bizkredit.module4.entity.Drawdown;
import com.bizkredit.common.enums.DrawdownStatus;
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

    @EntityGraph(attributePaths = {
            "facility",
            "facility.application",
            "facility.application.business",
            "facility.business"
    })
    List<Drawdown> findByStatus(DrawdownStatus status);
}