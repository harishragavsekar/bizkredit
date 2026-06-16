package com.bizkredit.repository;

import com.bizkredit.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByFacility_FacilityId(Long facilityId);

    List<Repayment> findByDrawdown_DrawdownId(Long drawdownId);

    // Sum of verified repayments for a drawdown (for outstanding calculation)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Repayment r " +
           "WHERE r.drawdown.drawdownId = :drawdownId " +
           "AND r.status != 'REVERSED'")
    BigDecimal sumRepaidForDrawdown(@Param("drawdownId") Long drawdownId);
}
