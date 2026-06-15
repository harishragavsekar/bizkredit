package com.bizkredit.repository;

import com.bizkredit.entity.CollateralRevaluation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollateralRevaluationRepository extends JpaRepository<CollateralRevaluation, Long> {

    @EntityGraph(attributePaths = {
            "collateral",
            "collateral.application",
            "collateral.application.business"
    })
    Optional<CollateralRevaluation> findById(Long id);

    @EntityGraph(attributePaths = {
            "collateral",
            "collateral.application",
            "collateral.application.business"
    })
    List<CollateralRevaluation> findByCollateral_CollateralId(Long collateralId);
}