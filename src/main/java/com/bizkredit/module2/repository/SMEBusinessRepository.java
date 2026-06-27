package com.bizkredit.module2.repository;

import com.bizkredit.module2.entity.SMEBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SMEBusinessRepository extends JpaRepository<SMEBusiness, Long> {

    Optional<SMEBusiness> findByRegistrationNumber(String registrationNumber);

    List<SMEBusiness> findByStatus(String status);

    List<SMEBusiness> findByIndustry(String industry);

    boolean existsByRegistrationNumber(String registrationNumber);
}
