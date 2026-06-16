package com.bizkredit.repository;

import com.bizkredit.entity.ScorecardModel;
import com.bizkredit.enums.ProductType;
import com.bizkredit.enums.ScorecardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScorecardModelRepository extends JpaRepository<ScorecardModel, Long> {

    List<ScorecardModel> findByStatus(ScorecardStatus status);

    // Returns list — use .isEmpty() check in service for uniqueness enforcement
    List<ScorecardModel> findByProductTypeAndStatus(ProductType productType, ScorecardStatus status);
}
