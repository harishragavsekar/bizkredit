package com.bizkredit.repository;

import com.bizkredit.entity.CollateralRevaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollateralRevaluationRepository extends JpaRepository<CollateralRevaluation, Long> {

    // Get all revaluations for a specific collateral asset
    List<CollateralRevaluation> findByCollateral_CollateralId(Long collateralId);
}
