package com.bizkredit.repository;

import com.bizkredit.entity.LoanApplication;
import com.bizkredit.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}