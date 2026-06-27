package com.bizkredit.module2.repository;

import com.bizkredit.module2.entity.GroupCompany;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupCompanyRepository extends JpaRepository<GroupCompany, Long> {

    // EntityGraph needed - parentBusiness/subsidiaryBusiness are LAZY and
    // open-in-view=false (same bug pattern as LoanApplicationRepository).
    @EntityGraph(attributePaths = {"parentBusiness", "subsidiaryBusiness"})
    Optional<GroupCompany> findById(Long id);

    @EntityGraph(attributePaths = {"parentBusiness", "subsidiaryBusiness"})
    List<GroupCompany> findByParentBusiness_BusinessId(Long parentBusinessId);
}
