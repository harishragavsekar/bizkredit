package com.bizkredit.repository;

import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.enums.FacilityStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityAccountRepository extends JpaRepository<FacilityAccount, Long> {

    @EntityGraph(attributePaths = {
            "application",
            "application.business",
            "business"
    })
    Optional<FacilityAccount> findById(Long id);

    @EntityGraph(attributePaths = {
            "application",
            "application.business",
            "business"
    })
    List<FacilityAccount> findByBusiness_BusinessId(Long businessId);

    List<FacilityAccount> findByStatus(FacilityStatus status);
}