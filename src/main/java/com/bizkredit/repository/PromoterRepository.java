package com.bizkredit.repository;

import com.bizkredit.entity.Promoter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoterRepository extends JpaRepository<Promoter, Long> {

    List<Promoter> findByBusiness_BusinessId(Long businessId);

    List<Promoter> findByStatus(String status);
}
