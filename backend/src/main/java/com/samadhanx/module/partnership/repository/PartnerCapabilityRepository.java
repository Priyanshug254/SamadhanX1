package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.PartnerCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartnerCapabilityRepository extends JpaRepository<PartnerCapability, UUID> {

    @Query("SELECT pc FROM PartnerCapability pc " +
            "JOIN pc.organization org " +
            "WHERE org.verificationStatus = 'VERIFIED'")
    List<PartnerCapability> findVerifiedPartnerCapabilities();
}
