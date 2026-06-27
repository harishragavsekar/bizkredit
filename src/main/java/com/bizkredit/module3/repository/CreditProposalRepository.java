package com.bizkredit.module3.repository;

import com.bizkredit.module3.entity.CreditProposal;
import com.bizkredit.common.enums.ProposalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditProposalRepository extends JpaRepository<CreditProposal, Long> {

    @EntityGraph(attributePaths = {"application", "application.business"})
    Optional<CreditProposal> findById(Long id);

    @EntityGraph(attributePaths = {"application", "application.business"})
    Optional<CreditProposal> findByApplication_ApplicationId(Long applicationId);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<CreditProposal> findAllByApplication_ApplicationId(Long applicationId);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<CreditProposal> findByStatus(ProposalStatus status);

    @EntityGraph(attributePaths = {"application", "application.business"})
    List<CreditProposal> findByAnalystId(Long analystId);
}