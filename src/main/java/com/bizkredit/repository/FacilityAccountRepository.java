package com.bizkredit.repository;

import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.enums.FacilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityAccountRepository extends JpaRepository<FacilityAccount, Long> {

    List<FacilityAccount> findByBusiness_BusinessId(Long businessId);

    List<FacilityAccount> findByStatus(FacilityStatus status);
}
