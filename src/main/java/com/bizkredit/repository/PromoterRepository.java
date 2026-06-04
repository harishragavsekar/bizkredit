package com.bizkredit.repository;

import com.bizkredit.entity.Promoter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoterRepository extends JpaRepository<Promoter, Long> {

    // Get all promoters for a specific business
    List<Promoter> findByBusiness_BusinessId(Long businessId);
}
