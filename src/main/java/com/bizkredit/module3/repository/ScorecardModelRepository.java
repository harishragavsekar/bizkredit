package com.bizkredit.module3.repository;

import com.bizkredit.module3.entity.ScorecardModel;
import com.bizkredit.common.enums.ProductType;
import com.bizkredit.common.enums.ScorecardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScorecardModelRepository extends JpaRepository<ScorecardModel, Long> {

    List<ScorecardModel> findByStatus(ScorecardStatus status);

    // Returns list - use .isEmpty() check in service for uniqueness enforcement
    List<ScorecardModel> findByProductTypeAndStatus(ProductType productType, ScorecardStatus status);
}
