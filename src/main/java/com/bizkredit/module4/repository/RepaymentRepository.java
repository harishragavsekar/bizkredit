package com.bizkredit.module4.repository;

import com.bizkredit.module4.entity.Repayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    // EntityGraph needed on every method that returns Repayment directly to a controller -
    // drawdown/facility are LAZY and open-in-view=false, so without it Jackson hits
    // LazyInitializationException on serialization (same bug as LoanApplicationRepository).
    @EntityGraph(attributePaths = {"drawdown", "facility"})
    Optional<Repayment> findById(Long id);

    @EntityGraph(attributePaths = {"drawdown", "facility"})
    List<Repayment> findByFacility_FacilityId(Long facilityId);

    @EntityGraph(attributePaths = {"drawdown", "facility"})
    List<Repayment> findByDrawdown_DrawdownId(Long drawdownId);

    // Sum of verified repayments for a drawdown (for outstanding calculation)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Repayment r " +
           "WHERE r.drawdown.drawdownId = :drawdownId " +
           "AND r.status != 'REVERSED'")
    BigDecimal sumRepaidForDrawdown(@Param("drawdownId") Long drawdownId);
}
