package com.bizkredit.repository;

import com.bizkredit.entity.FacilityAccount;
import com.bizkredit.enums.FacilityStatus;
import com.bizkredit.enums.ProductType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityAccountRepository extends JpaRepository<FacilityAccount, Long> {

    @EntityGraph(attributePaths = {"application", "application.business", "business"})
    Optional<FacilityAccount> findById(Long id);

    @EntityGraph(attributePaths = {"application", "application.business", "business"})
    List<FacilityAccount> findByBusiness_BusinessId(Long businessId);

    @EntityGraph(attributePaths = {"application", "application.business", "business"})
    List<FacilityAccount> findByStatus(FacilityStatus status);

    // Filtered query for GET /api/facilities
    @EntityGraph(attributePaths = {"application", "application.business", "business"})
    @Query("SELECT f FROM FacilityAccount f WHERE " +
           "(:businessId IS NULL OR f.business.businessId = :businessId) AND " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:productType IS NULL OR f.productType = :productType)")
    List<FacilityAccount> findWithFilters(
            @Param("businessId") Long businessId,
            @Param("status") FacilityStatus status,
            @Param("productType") ProductType productType
    );

    // Facilities expiring within N days (for renewal pipeline)
    @EntityGraph(attributePaths = {"application", "application.business", "business"})
    @Query("SELECT f FROM FacilityAccount f WHERE " +
           "f.status = 'ACTIVE' AND " +
           "f.expiryDate BETWEEN :now AND :cutoff " +
           "ORDER BY f.expiryDate ASC")
    List<FacilityAccount> findExpiringFacilities(
            @Param("now") LocalDate now,
            @Param("cutoff") LocalDate cutoff
    );

    // Portfolio analytics queries
    @Query("SELECT SUM(f.sanctionedLimit), SUM(f.outstandingBalance), COUNT(f) " +
           "FROM FacilityAccount f WHERE f.status = 'ACTIVE'")
    Object[] getPortfolioSummary();

    @Query("SELECT f.status, COUNT(f), SUM(f.outstandingBalance) " +
           "FROM FacilityAccount f GROUP BY f.status")
    List<Object[]> getAssetQualityDistribution();

    @Query("SELECT b.industry, SUM(f.outstandingBalance) " +
           "FROM FacilityAccount f JOIN f.business b " +
           "WHERE f.status = 'ACTIVE' " +
           "GROUP BY b.industry ORDER BY SUM(f.outstandingBalance) DESC")
    List<Object[]> getSectorExposure();
}
