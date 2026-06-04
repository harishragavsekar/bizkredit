package com.bizkredit.repository;

import com.bizkredit.entity.CreditProposal;
import com.bizkredit.enums.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditProposalRepository extends JpaRepository<CreditProposal, Long> {

    Optional<CreditProposal> findByApplication_ApplicationId(Long applicationId);

    List<CreditProposal> findByStatus(ProposalStatus status);

    List<CreditProposal> findByAnalystId(Long analystId);
}
