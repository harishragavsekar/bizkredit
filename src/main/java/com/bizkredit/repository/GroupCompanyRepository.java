package com.bizkredit.repository;

import com.bizkredit.entity.GroupCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCompanyRepository extends JpaRepository<GroupCompany, Long> {

    List<GroupCompany> findByParentBusiness_BusinessId(Long parentBusinessId);

    List<GroupCompany> findBySubsidiaryBusiness_BusinessId(Long subsidiaryBusinessId);
}
