package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.InstitutionalResource;
import com.samadhanx.module.organization.entity.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstitutionalResourceRepository extends JpaRepository<InstitutionalResource, UUID> {
    List<InstitutionalResource> findByOrganizationId(UUID organizationId);
    List<InstitutionalResource> findByOrganizationIdAndResourceType(UUID organizationId, ResourceType resourceType);
    List<InstitutionalResource> findByAccessibleToExternalTeamsTrue();
}
