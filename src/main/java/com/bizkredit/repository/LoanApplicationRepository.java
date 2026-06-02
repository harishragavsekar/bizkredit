package com.bizkredit.repository;

import com.bizkredit.entity.LoanApplication;
import com.bizkredit.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByBusiness_BusinessId(Long businessId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByAssignedAnalystId(Long analystId);
}
