package com.bizkredit.repository;

import com.bizkredit.entity.ApplicationDocument;
import com.bizkredit.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findByApplication_ApplicationId(Long applicationId);

    List<ApplicationDocument> findByVerificationStatus(VerificationStatus status);
}
