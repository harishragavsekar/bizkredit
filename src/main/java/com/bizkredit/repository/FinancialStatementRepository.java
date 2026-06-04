package com.bizkredit.repository;

import com.bizkredit.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    // Get all financial statements for a loan application (multi-year data)
    List<FinancialStatement> findByApplication_ApplicationId(Long applicationId);
}
