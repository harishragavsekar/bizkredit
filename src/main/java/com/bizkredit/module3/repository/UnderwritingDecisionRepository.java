package com.bizkredit.module3.repository;

import com.bizkredit.module3.entity.UnderwritingDecision;
import com.bizkredit.enums.DecisionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnderwritingDecisionRepository extends JpaRepository<UnderwritingDecision, Long> {

    @EntityGraph(attributePaths = {
            "proposal",
            "proposal.application",
            "proposal.application.business"
    })
    Optional<UnderwritingDecision> findById(Long id);

    @EntityGraph(attributePaths = {
            "proposal",
            "proposal.application",
            "proposal.application.business"
    })
    Optional<UnderwritingDecision> findByProposal_ProposalId(Long proposalId);

    @EntityGraph(attributePaths = {
            "proposal",
            "proposal.application",
            "proposal.application.business"
    })
    List<UnderwritingDecision> findByStatus(DecisionStatus status);

    @EntityGraph(attributePaths = {
            "proposal",
            "proposal.application",
            "proposal.application.business"
    })
    List<UnderwritingDecision> findByManagerId(Long managerId);
}