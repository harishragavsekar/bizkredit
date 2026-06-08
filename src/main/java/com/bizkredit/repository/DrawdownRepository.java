package com.bizkredit.repository;

import com.bizkredit.entity.Drawdown;
import com.bizkredit.enums.DrawdownStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawdownRepository extends JpaRepository<Drawdown, Long> {

    List<Drawdown> findByFacility_FacilityId(Long facilityId);

    List<Drawdown> findByStatus(DrawdownStatus status);
}
