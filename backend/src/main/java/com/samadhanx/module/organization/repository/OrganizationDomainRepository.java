package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.OrganizationDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationDomainRepository extends JpaRepository<OrganizationDomain, UUID> {
    List<OrganizationDomain> findByOrganizationId(UUID organizationId);
    void deleteByOrganizationId(UUID organizationId);
}
