package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.SupportingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportingDocumentRepository extends JpaRepository<SupportingDocument, UUID> {
    List<SupportingDocument> findByOrganizationId(UUID organizationId);
    List<SupportingDocument> findByVerificationRequestId(UUID verificationRequestId);
}
