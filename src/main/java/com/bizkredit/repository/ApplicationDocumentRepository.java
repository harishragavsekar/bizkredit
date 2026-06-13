package com.bizkredit.repository;

import com.bizkredit.entity.ApplicationDocument;
import com.bizkredit.enums.DocumentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    @EntityGraph(attributePaths = {"application", "application.business"})
    Optional<ApplicationDocument> findById(Long id);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<ApplicationDocument> findByApplication_ApplicationId(Long applicationId);
}