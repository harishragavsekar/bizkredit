package com.bizkredit.repository;

import com.bizkredit.entity.CovenantTemplate;
import com.bizkredit.enums.CovenantTemplateStatus;
import com.bizkredit.enums.CovenantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CovenantTemplateRepository extends JpaRepository<CovenantTemplate, Long> {

    boolean existsByTemplateName(String templateName);

    List<CovenantTemplate> findByStatus(CovenantTemplateStatus status);

    List<CovenantTemplate> findByCovenantTypeAndStatus(CovenantType covenantType, CovenantTemplateStatus status);
}
