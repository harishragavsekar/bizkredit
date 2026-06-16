package com.bizkredit.repository;

import com.bizkredit.entity.LoanApplication;
import com.bizkredit.enums.ApplicationStatus;
import com.bizkredit.enums.ProductType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    @EntityGraph(attributePaths = {"business"})
    Optional<LoanApplication> findById(Long id);

    @EntityGraph(attributePaths = {"business"})
    List<LoanApplication> findByBusiness_BusinessId(Long businessId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByAssignedAnalystId(Long analystId);

    // Filtered query for GET /api/applications
    @Query("SELECT a FROM LoanApplication a WHERE " +
           "(:businessId IS NULL OR a.business.businessId = :businessId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:productType IS NULL OR a.productType = :productType)")
    List<LoanApplication> findWithFilters(
            @Param("businessId") Long businessId,
            @Param("status") ApplicationStatus status,
            @Param("productType") ProductType productType
    );

    List<LoanApplication> findByRenewedFromFacilityId(Long facilityId);
}
