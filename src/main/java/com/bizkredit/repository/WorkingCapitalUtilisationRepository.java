package com.bizkredit.repository;

import com.bizkredit.entity.WorkingCapitalUtilisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingCapitalUtilisationRepository extends JpaRepository<WorkingCapitalUtilisation, Long> {

    List<WorkingCapitalUtilisation> findByFacility_FacilityId(Long facilityId);
}
