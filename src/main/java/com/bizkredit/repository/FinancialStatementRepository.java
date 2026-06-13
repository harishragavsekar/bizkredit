package com.bizkredit.repository;

import com.bizkredit.entity.FinancialStatement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    @EntityGraph(attributePaths = {"application", "application.business"})
    Optional<FinancialStatement> findById(Long id);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<FinancialStatement> findByApplication_ApplicationId(Long applicationId);
}