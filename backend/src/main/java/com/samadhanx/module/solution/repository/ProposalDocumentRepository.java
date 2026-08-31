package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.ProposalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProposalDocumentRepository extends JpaRepository<ProposalDocument, UUID> {
    List<ProposalDocument> findByProposalId(UUID proposalId);
}
