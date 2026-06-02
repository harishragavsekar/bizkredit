package com.bizkredit.repository;

import com.bizkredit.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    List<FinancialStatement> findByApplication_ApplicationId(Long applicationId);

    List<FinancialStatement> findByStatus(String status);
}
